package com.magicvector.common.basic.cache.impl;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.util.Asserts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Local Cache Implement.
 * TODO 未实现Set相关方法
 */
@Service("localCache")
@Slf4j
public class LocalCache extends AbstractCache {


    private  Cache<String, String> innerCache = null;

    public LocalCache(){
        // 获取 JVM 的最大可用内存
        long maxMemory = Runtime.getRuntime().maxMemory();
        long cacheMaxMemory = maxMemory / 5;
        // 创建带有最大内存限制的缓存
        innerCache = CacheBuilder.newBuilder()
                .maximumWeight(cacheMaxMemory)
                .weigher((String key, String value) -> getObjectSize(key) + getObjectSize(value))  // 计算条目所占内存的权重
                .build();
    }

    private static int getObjectSize(String str) {
        if (str == null) {
            return 0;
        }
        return str.length() * 2;  // 每个字符 2 字节
    }


    @Override
    protected void doHashSet(String hashName, String key, String value) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    protected void doSet(String key, String value, Long lifetime) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    protected void doPublish(String key, String value) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }


    @Override
    protected String doHashGet(String hashName, String key) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    protected long doHashDel(String hashName, String... keys) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    protected Map<String, String> doHashGetAll(String hashName) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    protected Object doGet(String key, Long lifetime) {
        try {
            Asserts.assertTrue( lifetime == null, Errors.NOT_SUPPORTED, "Local cache does not support resetting lifetime.");
            return ((LoadingCache)innerCache).get(key);
        } catch (ExecutionException e) {
            log.error("Error occurs while get key from the local cache.");
            return null;
        }
    }

    @Override
    protected boolean isLocal() {
        return true;
    }



    @Override
    public Long getLifetime(String key) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    public void remove(String key) {
        innerCache.invalidate(key);
    }

    @Override
    public long hsize(String hkey) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    public Long increase(String key) {
        Object value = get(key);
        if( value instanceof  Integer ){
           set(key,(int)value + 1);
        }
        else if( value instanceof  Integer ){
            set(key,(long)value + 1);
        }
        throw new MagicException(Errors.BAT_DATA_FORMAT, "The key must be an integer or long value.");
    }

    @Override
    public Long decrease(String key) {
        Object value = get(key);
        if( value instanceof  Integer ){
            set(key,(int)value - 1);
        }
        else if( value instanceof  Integer ){
            set(key,(long)value - 1);
        }
        throw new MagicException(Errors.BAT_DATA_FORMAT, "The key must be an integer or long value.");
    }

    @Override
    public Long increaseBy(String key, int k) {
        Object value = get(key);
        if( value instanceof  Integer ){
            set(key,(int)value + k);
        }
        else if( value instanceof  Integer ){
            set(key,(long)value + (long) k);
        }
        throw new MagicException(Errors.BAT_DATA_FORMAT, "The key must be an integer or long value.");
    }

    @Override
    public Long decreaseBy(String key, int k) {
        Object value = get(key);
        if( value instanceof  Integer ){
            set(key,(int)value + k);
        }
        else if( value instanceof  Integer ){
            set(key,(long)value + (long) k);
        }
        throw new MagicException(Errors.BAT_DATA_FORMAT, "The key must be an integer or long value.");
    }

    /**
     * Add one or more members to a set
     *
     * @param key
     * @param members
     * @return
     */
    @Override
    public Long sAdd(String key, String... members) {
        return null;
    }

    /**
     * get the number of members in a set
     *
     * @param key
     * @return
     */
    @Override
    public Long sCard(String key) {
        return null;
    }

    /**
     * get all members in a set.
     *
     * @param key
     * @return
     */
    @Override
    public Set<String> sMembers(String key) {
        return null;
    }

    /**
     * subtract multiple sets
     *
     * @param keys@return
     */
    @Override
    public Set<String> sDiff(String... keys) {
        return null;
    }

    /**
     * Add multiple sets
     *
     * @param keys
     * @return
     */
    @Override
    public Set<String> sUnion(String... keys) {
        return null;
    }

    /**
     * remove one or more members from a set
     *
     * @param key
     * @param members
     * @return
     */
    @Override
    public Long sRem(String key, String... members) {
        return null;
    }

    /**
     * determine if a given value is a member of a set
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Boolean sIsMember(String key, String member) {
        return null;
    }

    /**
     * intersect multiple sets
     *
     * @param keys
     * @return
     */
    @Override
    public Set<String> sInter(String... keys) {
        return null;
    }

    /**
     * move a member from one set to another
     *
     * @param sourceKey
     * @param destKey
     * @param member
     * @return
     */
    @Override
    public Long sMove(String sourceKey, String destKey, String member) {
        return null;
    }

    /**
     * @param key
     * @param fields
     * @return
     */
    @Override
    public List<String> hmget(String key, String... fields) {
        return null;
    }
}
