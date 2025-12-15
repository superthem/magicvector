package cn.magicvector.common.basic.cache.impl;

import com.github.tbwork.anole.loader.Anole;
import cn.magicvector.common.basic.cache.Cache;
import cn.magicvector.common.basic.cache.RepoCallback;
import cn.magicvector.common.basic.cache.Serializer;
import cn.magicvector.common.basic.cache.impl.serializer.*;
import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;
import cn.magicvector.common.basic.util.Asserts;
import cn.magicvector.common.basic.util.S;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Common implement of cache.
 */
public abstract class AbstractCache implements Cache {


    private static final Long cachedUpdateWindow = Anole.getLongProperty("cache.update.window", 500);//ms
    // 缓存值的包装结构
    private static class  CacheWrapper<T> {
        final long lastUpdateTime;
        final T cacheValue;

        CacheWrapper(long lastUpdateTime, T cacheValue) {
            this.lastUpdateTime = lastUpdateTime;
            this.cacheValue = cacheValue;
        }
    }
    protected static final String LOCAL_CAHCE_TYPE = "local";
    protected static final String DISTRIBUTED_CACHE_TYPE = "global";

    private Serializer serializer;

    public AbstractCache(){
        serializer = SerializerFactory.getSerializer();
    }

    private final static Set<Class> prototypeClasseSet = new HashSet<>();

    static {

        prototypeClasseSet.add(Integer.class );
        prototypeClasseSet.add(Short.class );
        prototypeClasseSet.add(Long.class);

    }

    public static class SerializerFactory{

        public static Serializer getSerializer(){
            String name = Anole.getProperty("cache.serializer","kryo");
            return getSerializer(name);
        }

        /**
         * @param name
         * @return
         */
        private static Serializer getSerializer(String name){
            switch (name){
                case "gson":{
                    return new GsonSerializer();
                }
                case "jackson":{
                    return new JacksonSerializer();
                }
                case "kryo":{
                    return new KryoSerializer();
                }
                default:
                    throw new MagicException(Errors.NOT_EXIST, "Could not find the appropriate serializer named {}, please set a right serializer via 'cache.serializer=xxx'", name);
            }
        }
    }

    @Override
    public void hset(String hashName, String key, Object value) {
        String serializedValue = serialize(value);
        doHashSet(hashName, key, serializedValue);
    }

    @Override
    public void set(String key, Object value, long lifetime) {
        String stringValue = serialize(value);
        doSet(key, stringValue, lifetime);
    }

    @Override
    public void set(String key, Object value) {
        String stringValue = serialize(value);
        doSet(key, stringValue, null);
    }


    /**
     * Set the value in the concurrent scenario.
     * Use the lastUpdateTime to replace the delay-double-remove
     * @param key
     * @param value
     * @param lifetime
     */
    @Override
    public void concurrentSet(String key, Object value, Long lifetime) {
        // 创建包装对象，记录当前时间
        CacheWrapper wrapper = new CacheWrapper(System.currentTimeMillis(), value);
        // 写入缓存（底层 doSet 会序列化）
        String stringValue = serialize(wrapper);
        doSet(key, stringValue, lifetime);
    }

    @Override
    public <T> T get(String key) {
        Object cacheValue = doGet(key, null);
        if(cacheValue == null){
            return null;
        }
        return (T) deserialize((String)cacheValue);
    }

    /**
     * Get the value in the concurrent scenario.
     * Use the lastUpdateTime to replace the delay-double-remove
     */
    @Override
    public <T> T concurrentGet(String key, RepoCallback<T> callback) {
        // 1. 先从缓存读
        Object cacheWrapperFirstStr = doGet(key, null);
        if (cacheWrapperFirstStr != null) {
            // 如果命中，直接返回包装内的值
            CacheWrapper<T> cacheWrapperFirst = (CacheWrapper<T>) deserialize((String)cacheWrapperFirstStr);
            return cacheWrapperFirst.cacheValue;
        }

        long start = System.currentTimeMillis();

        // 2. 缓存 miss
        T newValue = callback.retrieve();

        // 3. 准备写回前，再读一次缓存（二次读）
        Object cachedWrapperAgainStr = doGet(key, null);

        long end = System.currentTimeMillis();
        if (cachedWrapperAgainStr != null) {

            CacheWrapper<T> cacheWrapperAgain = (CacheWrapper<T>) deserialize((String)cachedWrapperAgainStr);
            T cacheValue = cacheWrapperAgain.cacheValue;
            Long cacheUpdateTime = cacheWrapperAgain.lastUpdateTime;

            // 在已经有缓存的情况下，如果当前请求是一个超时请求，就不用在更新缓存了。
            long elapsed = end - start;
            if(elapsed > cachedUpdateWindow * 2 && cacheUpdateTime > start){
                //非常慢的查询，数据可能已经更改了，直接返回查询结果，不再判断是否更新缓存。
                return newValue;
            }

            long now = System.currentTimeMillis();
            if (now - cacheUpdateTime < cachedUpdateWindow) { //一般来说500毫秒中
                // 缓存刚被更新，放弃回填旧值
                return cacheValue;
            }
            // 否则可以安全写回
        }

        // 4. 写回缓存
        CacheWrapper<T> newWrapper = new CacheWrapper<T>(System.currentTimeMillis(), newValue);
        set(key, newWrapper);

        return newValue;
    }

    @Override
    public void publish(String channel, String message) {
        String stringValue = serialize(message);
        doPublish(channel, stringValue);
    }


    @Override
    public <T> T hget(String cacheGroup, String key) {
        String cacheValue = doHashGet(cacheGroup, key);
        if(cacheValue == null){
            return null;
        }
        return (T) deserialize(cacheValue);
    }

    @Override
    public long hdel(String cacheGroup, String... keys) {
        return doHashDel(cacheGroup, keys);
    }

    @Override
    public <T> HashMap<String, T> hgetAll(String cacheGroup) {
        Map<String, String> cacheValueMap = doHashGetAll(cacheGroup);
        HashMap<String, T> resultMap = new HashMap<>();
        if(cacheValueMap == null){
            return resultMap;
        }
        cacheValueMap.forEach((k,v) ->{
            resultMap.put(k,(T) deserialize(v));
        });
        return resultMap;
    }


    @Override
    public <T> T getAndExpire(String key, long lifetime) {
        Object cacheValue = doGet(key, lifetime);
        if(cacheValue == null){
            return null;
        }
        return (T)  deserialize((String)cacheValue);
    }



    protected abstract void doHashSet(String hashName, String key, String value);

    protected abstract void doSet(String key, String value, Long lifetime);

    protected abstract void doPublish(String key, String value);

    protected abstract String doHashGet(String hashName, String key);

    protected abstract long doHashDel(String hashName, String... keys);

    protected abstract Map<String, String> doHashGetAll(String hashName);

    protected abstract Object doGet(String key, Long lifetime);



    private String serialize(Object obj){
        if(isPrototype(obj)){
            return obj.toString();
        }
        return serializer.serialize(obj);
    }

    protected Object deserialize(String objStr){
        if(isInteger(objStr)){
            return Long.valueOf(objStr).intValue();
        }
        return serializer.deserialize(objStr);
    }

    private boolean isPrototype(Object obj){
       return prototypeClasseSet.contains(obj.getClass());
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}
