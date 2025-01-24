package com.magicvector.common.basic.cache.impl;

import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.basic.cache.Cache;
import com.magicvector.common.basic.cache.Serializer;
import com.magicvector.common.basic.cache.impl.serializer.*;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.util.Asserts;
import com.magicvector.common.basic.util.S;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Common implement of cache.
 */
public abstract class AbstractCache implements Cache {

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

    @Override
    public <T> T get(String key) {
        Object cacheValue = doGet(key, null);
        if(cacheValue == null){
            return null;
        }
        return (T) deserialize((String)cacheValue);
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
