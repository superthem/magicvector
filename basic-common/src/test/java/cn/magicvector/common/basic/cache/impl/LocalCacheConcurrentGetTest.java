package cn.magicvector.common.basic.cache.impl;

import cn.magicvector.common.basic.cache.RepoCallback;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link LocalCache#concurrentGet} 高并发正确性：同 key 回源应单飞，结果一致。
 * <p>
 * 本测试不启动 Anole：{@link AbstractCache} 在 Anole 未就绪时使用默认窗口与 kryo。
 * <p>
 * Maven：{@code mvn -pl basic-common test -Dtest=LocalCacheConcurrentGetTest}<br>
 * 演示日志：运行 {@link #main(String[])}（含 {@link #demonstrateLifetimeTtlSeconds()} TTL 自测）。
 */
public class LocalCacheConcurrentGetTest {

    /** main 自测用：短 TTL，便于肉眼等 sleep */
    private static final int TTL_VERIFY_SECONDS = 2;

    private static final int STRESS_THREADS = 128;
    private static final int STRESS_ROUNDS = 5;

    /** main 演示用：线程数少便于阅读日志 */
    private static final int DEMO_THREADS = 12;
    private static final int DEMO_RETRIEVE_SLEEP_MS = 250;

    @Test
    public void sameKeyHighConcurrency_singleRetrieve_allSameValue() throws Exception {
        for (int round = 0; round < STRESS_ROUNDS; round++) {
            runSameKeyBurst(STRESS_THREADS, round);
        }
    }

    @Test
    public void distinctKeys_parallelLoads_noCrossKeyBlocking() throws Exception {
        LocalCache cache = new LocalCache();
        int n = 32;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        AtomicInteger totalLoads = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            for (int i = 0; i < n; i++) {
                final String key = "distinct-" + i + "-" + System.nanoTime();
                final int seed = i;
                pool.submit(() -> {
                    try {
                        start.await();
                        String v = cache.concurrentGet(key, new RepoCallback<String>() {
                            @Override
                            public String retrieve() {
                                totalLoads.incrementAndGet();
                                return "v-" + seed;
                            }
                        });
                        Assert.assertEquals("v-" + seed, v);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Assert.fail(e.getMessage());
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            Assert.assertTrue(done.await(120, TimeUnit.SECONDS));
            Assert.assertEquals(n, totalLoads.get());
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * 批量请求同一 key：打印每个请求进入 moment、leader 回源过程、以及各自返回耗时与角色。
     */
    public static void demonstrateBatchRequestsWithWaitLogs() throws InterruptedException {
        System.out.println("========== concurrentGet 演示（单 key，多请求） ==========");
        LocalCache cache = new LocalCache();
        String key = "demo-key-" + System.nanoTime();
        final String payload = "value-for-" + key;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(DEMO_THREADS);
        AtomicInteger loadCount = new AtomicInteger(0);
        AtomicReference<String> leaderThreadName = new AtomicReference<>();

        ExecutorService pool = Executors.newFixedThreadPool(DEMO_THREADS);
        try {
            for (int i = 0; i < DEMO_THREADS; i++) {
                final int reqId = i + 1;
                pool.submit(() -> {
                    String tname = Thread.currentThread().getName();
                    try {
                        start.await();
                        logLine(String.format("请求 #%d [%s] 进入 concurrentGet（阻塞直至有结果）", reqId, tname));
                        long t0 = System.currentTimeMillis();

                        String v = cache.concurrentGet(key, new RepoCallback<String>() {
                            @Override
                            public String retrieve() {
                                loadCount.incrementAndGet();
                                leaderThreadName.compareAndSet(null, Thread.currentThread().getName());
                                logLine(String.format(
                                        "请求 #%d [%s] *** LEADER：执行 retrieve() / 模拟回源 ~%d ms ***",
                                        reqId, Thread.currentThread().getName(), DEMO_RETRIEVE_SLEEP_MS));
                                sleepQuiet(DEMO_RETRIEVE_SLEEP_MS);
                                return payload;
                            }
                        });

                        long elapsed = System.currentTimeMillis() - t0;
                        boolean isLeader = tname.equals(leaderThreadName.get());
                        logLine(String.format(
                                "请求 #%d [%s] 返回：耗时=%d ms，值=%s，角色=%s",
                                reqId, tname, elapsed, v, isLeader ? "LEADER" : "FOLLOWER（等待 leader 完成）"));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logLine("请求 #" + reqId + " [" + tname + "] 被中断");
                    } finally {
                        done.countDown();
                    }
                });
            }
            logLine("主线程：发令同时进入 " + DEMO_THREADS + " 个请求…");
            start.countDown();
            done.await(120, TimeUnit.SECONDS);
            System.out.println("---------- 校验：retrieve 应仅 1 次，实际 " + loadCount.get() + " ----------");
            if (loadCount.get() != 1) {
                throw new IllegalStateException("单飞失效：retrieve 次数应为 1");
            }
        } finally {
            pool.shutdownNow();
        }
    }

    private static void runSameKeyBurst(int threads, int round) throws InterruptedException {
        LocalCache cache = new LocalCache();
        String key = "same-key-" + round + "-" + System.nanoTime();
        AtomicInteger loadCount = new AtomicInteger(0);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<AtomicReference<String>> results = new ArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for (int i = 0; i < threads; i++) {
                AtomicReference<String> slot = new AtomicReference<>();
                results.add(slot);
                pool.submit(() -> {
                    try {
                        start.await();
                        String v = cache.concurrentGet(key, new RepoCallback<String>() {
                            @Override
                            public String retrieve() {
                                loadCount.incrementAndGet();
                                sleepQuiet(30);
                                return "payload-" + key;
                            }
                        });
                        slot.set(v);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        slot.set("interrupted");
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            Assert.assertTrue("round " + round + " timeout", done.await(120, TimeUnit.SECONDS));
            Assert.assertEquals("round " + round + ": retrieve 应只执行一次", 1, loadCount.get());
            String expected = "payload-" + key;
            for (int i = 0; i < results.size(); i++) {
                Assert.assertEquals("round " + round + " thread " + i, expected, results.get(i).get());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * 手跑 main 时验证：{@code concurrentGet(..., ttlSec)} 写入后，{@code ttlSec} 秒内命中不触发 retrieve，
     * 过期后再调应重新 retrieve。
     */
    public static void demonstrateLifetimeTtlSeconds() throws InterruptedException {
        System.out.println("========== lifetime（秒）TTL： miss → hit → 过期后再 miss ==========");
        LocalCache cache = new LocalCache();
        String key = "ttl-verify-" + System.nanoTime();
        AtomicInteger loads = new AtomicInteger(0);

        String v1 = cache.concurrentGet(key, new RepoCallback<String>() {
            @Override
            public String retrieve() {
                int n = loads.incrementAndGet();
                System.out.println("[ttl] retrieve 执行，第 " + n + " 次（应只见 1、2 两次）");
                return "payload-" + n;
            }
        }, (long) TTL_VERIFY_SECONDS);
        System.out.println("[ttl] 第 1 次返回: " + v1 + "，loads=" + loads.get() + " 预期=1");

        String v2 = cache.concurrentGet(key, new RepoCallback<String>() {
            @Override
            public String retrieve() {
                loads.incrementAndGet();
                return "BUG-should-not-retrieve";
            }
        }, (long) TTL_VERIFY_SECONDS);
        System.out.println("[ttl] 第 2 次（立即）返回: " + v2 + "，loads=" + loads.get() + " 预期=1，与第1次同值");

        Long remain = cache.getLifetime(key);
        System.out.println("[ttl] getLifetime（秒）≈ " + remain + "（刚写入时应 >0）");

        int waitSec = TTL_VERIFY_SECONDS + 1;
        System.out.println("[ttl] sleep " + waitSec + "s 等 key 过期…");
        Thread.sleep(waitSec * 1000L);

        String v3 = cache.concurrentGet(key, new RepoCallback<String>() {
            @Override
            public String retrieve() {
                int n = loads.incrementAndGet();
                System.out.println("[ttl] retrieve 执行，第 " + n + " 次");
                return "payload-" + n;
            }
        }, (long) TTL_VERIFY_SECONDS);
        System.out.println("[ttl] 第 3 次（过期后）返回: " + v3 + "，loads=" + loads.get() + " 预期=2");

        if (loads.get() != 2) {
            throw new IllegalStateException("lifetime 未按预期生效：期望 retrieve 共 2 次，实际 " + loads.get());
        }
        if (!v1.equals(v2) || v1.equals(v3)) {
            throw new IllegalStateException("值异常：预期 v1==v2 且 v3 为二次加载");
        }
        System.out.println("[ttl] OK");
    }

    private static void logLine(String msg) {
        long now = System.currentTimeMillis();
        System.out.printf("[%1$tT.%1$tL] %2$s%n", now, msg);
    }

    private static void sleepQuiet(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        demonstrateLifetimeTtlSeconds();

        demonstrateBatchRequestsWithWaitLogs();

        LocalCacheConcurrentGetTest t = new LocalCacheConcurrentGetTest();
        long t0 = System.currentTimeMillis();
        t.sameKeyHighConcurrency_singleRetrieve_allSameValue();
        t.distinctKeys_parallelLoads_noCrossKeyBlocking();
        System.out.println("JUnit 校验通过，耗时 " + (System.currentTimeMillis() - t0) + " ms");
    }
}
