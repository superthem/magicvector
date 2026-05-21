package cn.magicvector.common.basic.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;
import cn.magicvector.common.basic.util.Asserts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Local Cache Implement.
 */
@Service("localCache")
@Slf4j
public class LocalCache extends AbstractCache {

    /**
     * Guava {@link CacheBuilder#expireAfterWrite} 只能全局统一，无法按 key 设置不同 TTL。
     * 因此在条目中自带过期时间，读时校验；{@code expireAtMillis == Long.MAX_VALUE} 表示与 Redis 无 EX 一致，不设时钟过期。
     */
    private static final class ExpiringEntry {
        final long expireAtMillis;
        final String payload;

        ExpiringEntry(long expireAtMillis, String payload) {
            this.expireAtMillis = expireAtMillis;
            this.payload = payload;
        }
    }

    private final Cache<String, ExpiringEntry> innerCache;
    private final Map<String, Map<String, String>> hashCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> setCache = new ConcurrentHashMap<>();

    public LocalCache() {
        // 获取 JVM 的最大可用内存
        long maxMemory = Runtime.getRuntime().maxMemory();
        long cacheMaxMemory = maxMemory / 5;
        // 创建带有最大内存限制的缓存；逐条过期时间见 ExpiringEntry，不再使用全局 expireAfterWrite
        innerCache = CacheBuilder.newBuilder()
                .maximumWeight(cacheMaxMemory)
                .weigher((String key, ExpiringEntry entry) ->
                        getObjectSize(key) + 16 + getObjectSize(entry.payload))
                .build();
    }

    private static int getObjectSize(String str) {
        if (str == null) {
            return 0;
        }
        return 8 + str.length() * 2; // 字符串对象头 + 每个字符 2 字节
    }

    private static long expiryMillisFromLifetimeSeconds(Long lifetimeSeconds) {
        if (lifetimeSeconds == null) {
            return Long.MAX_VALUE;
        }
        long add = TimeUnit.SECONDS.toMillis(Math.max(0L, lifetimeSeconds));
        long candidate = System.currentTimeMillis() + add;
        return candidate < 0 ? Long.MAX_VALUE : candidate;
    }

    @Override
    protected void doHashSet(String hashName, String key, String value) {
        hashCache.computeIfAbsent(hashName, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    @Override
    protected void doHashSetAll(String hashName, Map<String, String> fieldValues) {
        if (fieldValues.isEmpty()) {
            hashCache.remove(hashName);
            return;
        }
        hashCache.put(hashName, new ConcurrentHashMap<>(fieldValues));
    }

    /**
     * @param lifetime 过期时间（<b>秒</b>）；{@code null} 表示不设时钟过期（仅受 Guava 权重驱逐影响），与 Redis 无 TTL 相近。
     */
    @Override
    protected void doSet(String key, String value, Long lifetime) {
        innerCache.put(key, new ExpiringEntry(expiryMillisFromLifetimeSeconds(lifetime), value));
    }

    @Override
    protected void doPublish(String key, String value) {
        // 本地缓存不支持发布/订阅模式
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    protected String doHashGet(String hashName, String key) {
        return hashCache.getOrDefault(hashName, Collections.emptyMap()).get(key);
    }

    @Override
    protected long doHashDel(String hashName, String... keys) {
        Map<String, String> hash = hashCache.get(hashName);
        if (hash == null) {
            return 0;
        }
        long count = 0;
        for (String key : keys) {
            if (hash.remove(key) != null) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected Map<String, String> doHashGetAll(String hashName) {
        return new HashMap<>(hashCache.getOrDefault(hashName, Collections.emptyMap()));
    }

    /**
     * @param lifetime 非 {@code null} 时：命中且未过期则按该秒数重写过期时间（对齐 Redis 读后续期）；{@code null} 则只读。
     */
    @Override
    protected Object doGet(String key, Long lifetime) {
        ExpiringEntry entry = innerCache.getIfPresent(key);
        if (entry == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (now >= entry.expireAtMillis) {
            innerCache.invalidate(key);
            return null;
        }
        if (lifetime != null) {
            ExpiringEntry renewed = new ExpiringEntry(expiryMillisFromLifetimeSeconds(lifetime), entry.payload);
            innerCache.put(key, renewed);
            return renewed.payload;
        }
        return entry.payload;
    }


    /**
     * @return 剩余秒数；无 key 或已过期为 {@code null}；无时钟过期（与 Redis TTL -1 类似）为 {@code -1L}。
     */
    @Override
    public Long getLifetime(String key) {
        ExpiringEntry entry = innerCache.getIfPresent(key);
        if (entry == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (now >= entry.expireAtMillis) {
            innerCache.invalidate(key);
            return null;
        }
        if (entry.expireAtMillis == Long.MAX_VALUE) {
            return -1L;
        }
        long remainMs = entry.expireAtMillis - now;
        return Math.max(0L, (remainMs + 999L) / 1000L);
    }

    @Override
    public void remove(String key) {
        innerCache.invalidate(key);
    }

    @Override
    public long hsize(String hkey) {
        return hashCache.getOrDefault(hkey, Collections.emptyMap()).size();
    }

    @Override
    public Long increase(String key) {
        return increaseBy(key, 1);
    }

    @Override
    public Long decrease(String key) {
        return decreaseBy(key, 1);
    }

    @Override
    public Long increaseBy(String key, int k) {
        Object value = doGet(key, null);
        if (value == null) {
            value = 0L;
        }
        if (value instanceof Number) {
            long newValue = ((Number) value).longValue() + k;
            doSet(key, String.valueOf(newValue), null);
            return newValue;
        }
        throw new MagicException(Errors.BAD_DATA_FORMAT, "The key must be an integer or long value.");
    }

    @Override
    public Long decreaseBy(String key, int k) {
        return increaseBy(key, -k);
    }

    @Override
    public Long sAdd(String key, String... members) {
        Set<String> set = setCache.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        long count = 0;
        for (String member : members) {
            if (set.add(member)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Long sCard(String key) {
        return (long) setCache.getOrDefault(key, Collections.emptySet()).size();
    }

    @Override
    public Set<String> sMembers(String key) {
        return new HashSet<>(setCache.getOrDefault(key, Collections.emptySet()));
    }

    @Override
    public Set<String> sDiff(String... keys) {
        if (keys.length == 0) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>(setCache.getOrDefault(keys[0], Collections.emptySet()));
        for (int i = 1; i < keys.length; i++) {
            result.removeAll(setCache.getOrDefault(keys[i], Collections.emptySet()));
        }
        return result;
    }

    @Override
    public Set<String> sUnion(String... keys) {
        Set<String> result = new HashSet<>();
        for (String key : keys) {
            result.addAll(setCache.getOrDefault(key, Collections.emptySet()));
        }
        return result;
    }

    @Override
    public Long sRem(String key, String... members) {
        Set<String> set = setCache.get(key);
        if (set == null) {
            return 0L;
        }
        long count = 0;
        for (String member : members) {
            if (set.remove(member)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Boolean sIsMember(String key, String member) {
        return setCache.getOrDefault(key, Collections.emptySet()).contains(member);
    }

    @Override
    public Set<String> sInter(String... keys) {
        if (keys.length == 0) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>(setCache.getOrDefault(keys[0], Collections.emptySet()));
        for (int i = 1; i < keys.length; i++) {
            result.retainAll(setCache.getOrDefault(keys[i], Collections.emptySet()));
        }
        return result;
    }

    @Override
    public Long sMove(String sourceKey, String destKey, String member) {
        Set<String> sourceSet = setCache.get(sourceKey);
        if (sourceSet == null || !sourceSet.contains(member)) {
            return 0L;
        }
        sourceSet.remove(member);
        setCache.computeIfAbsent(destKey, k -> ConcurrentHashMap.newKeySet()).add(member);
        return 1L;
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        Map<String, String> hash = hashCache.get(key);
        if (hash == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String field : fields) {
            result.add(hash.getOrDefault(field, null));
        }
        return result;
    }

    @Override
    public Long lpush(String key, String... values) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    public Long rpush(String key, String... values) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    public String brpop(String key, int timeoutSeconds) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    public Boolean lcontains(String key, String element) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }
}