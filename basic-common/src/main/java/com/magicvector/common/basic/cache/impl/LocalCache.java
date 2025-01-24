package com.magicvector.common.basic.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.util.Asserts;
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

    private final Cache<String, String> innerCache;
    private final Map<String, Map<String, String>> hashCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> setCache = new ConcurrentHashMap<>();

    public LocalCache() {
        // 获取 JVM 的最大可用内存
        long maxMemory = Runtime.getRuntime().maxMemory();
        long cacheMaxMemory = maxMemory / 5;
        // 创建带有最大内存限制的缓存
        innerCache = CacheBuilder.newBuilder()
                .maximumWeight(cacheMaxMemory)
                .weigher((String key, String value) -> getObjectSize(key) + getObjectSize(value)) // 计算条目所占内存的权重
                .expireAfterWrite(60, TimeUnit.MINUTES) // 设置默认过期时间
                .build();
    }

    private static int getObjectSize(String str) {
        if (str == null) {
            return 0;
        }
        return 8 + str.length() * 2; // 字符串对象头 + 每个字符 2 字节
    }

    @Override
    protected void doHashSet(String hashName, String key, String value) {
        hashCache.computeIfAbsent(hashName, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    @Override
    protected void doSet(String key, String value, Long lifetime) {
        if (lifetime != null) {
            log.warn("LocalCache does not support setting individual TTL for keys. Ignoring lifetime.");
        }
        innerCache.put(key, value);
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

    @Override
    protected Object doGet(String key, Long lifetime) {
        if (lifetime != null) {
            log.warn("LocalCache does not support setting individual TTL for keys. Ignoring lifetime.");
        }
        return innerCache.getIfPresent(key);
    }


    @Override
    public Long getLifetime(String key) {
        // 本地缓存不支持获取过期时间
        throw new MagicException(Errors.NOT_SUPPORTED);
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
}