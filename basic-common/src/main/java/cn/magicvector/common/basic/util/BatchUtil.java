package cn.magicvector.common.basic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchUtil {

    private static final Logger log = LoggerFactory.getLogger(BatchUtil.class);

    /** 用于工作线程命名序号（勿在工厂里每次 new AtomicInteger，否则会一直得到 0）。 */
    private static final AtomicInteger DAEMON_THREAD_SEQUENCE = new AtomicInteger(0);

    private static final ThreadFactory PROGRESS_SCHEDULER_THREAD_FACTORY = r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("BatchUtil-Progress-Scheduler");
        return thread;
    };

    // 自定义一个守护线程的线程工厂
    private static final ThreadFactory DAEMON_THREAD_FACTORY = r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true); // 【核心】设置为守护线程
        // 顺便给线程起个好听的名字，方便排查问题
        thread.setName("BatchUtil-Daemon-Thread-" + DAEMON_THREAD_SEQUENCE.getAndIncrement());
        return thread;
    };

    // 队列必须能容纳单次 batch 的全部任务：有界队列在 invokeAll 提交满后会 RejectedExecutionException，
    // JDK 会在 invokeAll 的 finally 里对所有已创建的 Future cancel(true)，整池 worker 被 interrupt → OkHttp InterruptedIOException。
    private static final ExecutorService GLOBAL_EXECUTOR = new ThreadPoolExecutor(
            10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            DAEMON_THREAD_FACTORY // 传入我们自定义的守护线程工厂
    );

    /** 与 batchRunToEnd 互斥：已占用执行槽但尚未向线程池提交任务时，靠此标志拒绝并发调用。 */
    private static final AtomicBoolean BATCH_EXCLUSIVE_HELD = new AtomicBoolean(false);

    /** {@link #batchRunAndGetResultUntilSuccess} 在竞争不到执行槽时的轮询间隔（毫秒）。 */
    private static final long BATCH_BUSY_POLL_INTERVAL_MS = 1000L;

    public interface Callback<P, T> {
        T run(P param);
    }

    /**
     * 探测全局批量线程池（GLOBAL_EXECUTOR）是否仍有任务在执行或排队。
     */
    public static boolean isBusy() {
        ThreadPoolExecutor pool = (ThreadPoolExecutor) GLOBAL_EXECUTOR;
        return pool.getActiveCount() > 0 || !pool.getQueue().isEmpty();
    }

    /**
     * 反复调用 {@link #batchRunAndGetResult}；若因繁忙得到 null 则睡眠后继续竞争，直至执行完成并返回结果列表。
     *
     * @param maxWaitTimeSeconds 最长等待秒数，从进入本方法起算；为 0 表示不超时、直至成功。
     * @return params 为空时返回空列表；若在限时内仍未获得执行并完成，返回 null（代表依旧没有运行）。
     */
    public static <P, T> List<T> batchRunAndGetResultUntilSuccess(String taskName,
            List<P> params, Callback<P, T> callback, int maxWaitTimeSeconds) {
        if (params == null || params.isEmpty()) {
            return new ArrayList<>();
        }
        long deadlineNanos =
                maxWaitTimeSeconds <= 0
                        ? Long.MAX_VALUE
                        : System.nanoTime() + TimeUnit.SECONDS.toNanos(maxWaitTimeSeconds);
        for (;;) {
            List<T> result = batchRunAndGetResult(taskName, params, callback);
            if (result != null) {
                return result;
            }
            try {
                long sleepMs = BATCH_BUSY_POLL_INTERVAL_MS;
                if (maxWaitTimeSeconds > 0) {
                    long remainingMs = TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
                    if (remainingMs <= 0) {
                        return null;
                    }
                    sleepMs = Math.min(sleepMs, remainingMs);
                }
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("等待批量执行槽时被中断", e);
            }
        }
    }

    /**
     * 批量执行任务直至全部结束。
     *
     * @return 每任务结果列表；params 为空时返回空列表。若返回 null 代表正在处理任务、请稍等。
     */
    public static <P, T> List<T> batchRunAndGetResult(String taskName, List<P> params, Callback<P, T> callback) {
        if (params == null || params.isEmpty()) {
            return new ArrayList<>();
        }
        if (!tryAcquireExclusiveBatchRun()) {
            return null;
        }
        try {
            return doBatchRunToEnd(taskName, params, callback);
        } finally {
            releaseExclusiveBatchRun();
        }
    }

    /**
     * double-check：先快速判断线程池与互斥标志，再以 CAS 占位；占位成功后再次确认线程池无遗留任务。
     */
    private static boolean tryAcquireExclusiveBatchRun() {
        if (isBusy() || BATCH_EXCLUSIVE_HELD.get()) {
            return false;
        }
        if (!BATCH_EXCLUSIVE_HELD.compareAndSet(false, true)) {
            return false;
        }
        if (isBusy()) {
            BATCH_EXCLUSIVE_HELD.set(false);
            return false;
        }
        return true;
    }

    private static void releaseExclusiveBatchRun() {
        BATCH_EXCLUSIVE_HELD.set(false);
    }

    private static <P, T> List<T> doBatchRunToEnd(String taskName, List<P> params, Callback<P, T> callback) {
        int total = params.size();
        String waiterName = Thread.currentThread().getName();
        logBatchStart(waiterName, total);

        AtomicInteger completed = new AtomicInteger(0);
        ScheduledExecutorService progressScheduler =
                Executors.newSingleThreadScheduledExecutor(PROGRESS_SCHEDULER_THREAD_FACTORY);
        ScheduledFuture<?> progressFuture = progressScheduler.scheduleAtFixedRate(() -> {
            int c = completed.get();
            int pct = Math.min(100, c * 100 / total);
            log.info("[{}] 批量任务执行进度： {}% ({}/{})", taskName, pct, c, total);
        }, 5L, 5L, TimeUnit.SECONDS);

        try {
            return invokeAllAndGetResults(params, callback, completed);
        } catch (InterruptedException e) {
            throw interruptedFailure(waiterName, e);
        } catch (ExecutionException e) {
            throw executionFailure(waiterName, e);
        } finally {
            progressFuture.cancel(false);
            progressScheduler.shutdown();
            logBatchDone(completed, total);
        }
    }

    private static void logBatchStart(String waiterName, int total) {
        log.info("批处理开始，线程名={} 总任务数量={}", waiterName, total);
    }

    private static void logBatchDone(AtomicInteger completed, int total) {
        log.info(
                "批处理任务已经完成。 {}% ({}/{})",
                Math.min(100, completed.get() * 100 / total),
                completed.get(),
                total);
    }

    private static <P, T> List<T> invokeAllAndGetResults(
            List<P> params, Callback<P, T> callback, AtomicInteger completed)
            throws InterruptedException, ExecutionException {
        List<Callable<T>> tasks = buildTasks(params, callback, completed);
        List<Future<T>> futures = GLOBAL_EXECUTOR.invokeAll(tasks);
        List<T> results = new ArrayList<>(futures.size());
        for (Future<T> future : futures) {
            results.add(future.get());
        }
        return results;
    }

    private static <P, T> List<Callable<T>> buildTasks(
            List<P> params, Callback<P, T> callback, AtomicInteger completed) {
        List<Callable<T>> tasks = new ArrayList<>(params.size());
        for (P param : params) {
            tasks.add(() -> runOneTask(param, callback, completed));
        }
        return tasks;
    }

    private static <P, T> T runOneTask(P param, Callback<P, T> callback, AtomicInteger completed) {
        Thread worker = Thread.currentThread();
        if (worker.isInterrupted()) {
            log.warn(
                    "batchRunToEnd worker ENTERED with interrupt already set (upstream cancel/interrupt propagated) workerThread={}",
                    worker.getName());
        }
        try {
            return callback.run(param);
        } finally {
            completed.incrementAndGet();
            if (worker.isInterrupted()) {
                log.warn(
                        "batchRunToEnd worker LEAVES with interrupt=true (check stack for InterruptedIOException upstream) workerThread={}",
                        worker.getName());
            }
        }
    }

    private static RuntimeException interruptedFailure(String waiterName, InterruptedException e) {
        /*
         * 结论：在等待 invokeAll / future.get() 时被中断的是「调度 batchRunToEnd」的这条 waiter 线程，
         * 一般由框架 cancel 调度、容器关闭、或对这条线程 interrupt 引起。
         */
        log.warn(
                "batchRunToEnd INTERRUPT on waiter waiterThread={} (invokeAll/get interrupted)",
                waiterName,
                e);
        Thread.currentThread().interrupt();
        return new RuntimeException("批量执行任务时发生异常", e);
    }

    private static RuntimeException executionFailure(String waiterName, ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof InterruptedIOException) {
            /*
             * 结论：HTTP 写出了 InterruptedIOException——执行任务的 workerThread 在打日志时的 interrupt 语义见上一条 worker WARN。
             * 常与 Future.cancel(true) / pool.shutdownNow / 或对 worker 的直接 interrupt 同现。
             */
            log.warn(
                    "batchRunToEnd EXECUTION FAILED with InterruptedIOException waiterThread={} message={}",
                    waiterName,
                    cause.getMessage(),
                    cause);
        }
        return new RuntimeException("批量执行任务时发生异常", e);
    }
}
