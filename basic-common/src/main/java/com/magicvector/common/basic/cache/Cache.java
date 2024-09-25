package com.magicvector.common.basic.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface Cache {

    /**
     * Add field to the hash object, which is useful in some
     * situations like login info, user vouchers, etc..
     *
     * @param hashName the hash's name.
     * @param key      the key
     * @param value    the value.
     */
    void hset(String hashName, String key, Object value);

    /**
     * Set key-value to the cache.
     *
     * @param key      the key
     * @param value    the value.
     * @param lifetime the lifetime of key-value ( seconds )
     */
    void set(String key, Object value, long lifetime);

    /**
     * Set key-value to the cache without setting lifetime.
     *
     * @param key   the key
     * @param value the value
     */
    void set(String key, Object value);


    /**
     * Set channel-message to the cache
     *
     * @param channel   the channel
     * @param message  the message
     */
    void publish(String channel, String message);


    /**
     * Get value of the given hash and key.
     *
     * @param hashName the hash's name
     * @param key      the given key
     * @param <T>      the value's type
     * @return the value object.
     */
    <T> T hget(String hashName, String key);


    /**
     * delete value of the given hash and key.
     *
     * @param hashName the hash's name
     * @param keys     the given keys
     * @return the value object.
     */
    long hdel(String hashName, String... keys);


    /**
     * Get value of the given hash.
     *
     * @param hashName the hash's name
     * @param <T>      the value's type
     * @return the value object.
     */
    <T> HashMap<String, T> hgetAll(String hashName);


    /**
     * Get multiple values from a hash map.
     * @param key
     * @param fields
     * @return
     */
    List<String> hmget(String key, String... fields);



    /**
     * Get the size of hash list.
     *
     * @param hkey
     * @return
     */
    long hsize(String hkey);


    /**
     * Get value of the given key.
     *
     * @param key the given key
     * @param <T> the value's type
     * @return the value object.
     */
    <T> T get(String key);


    /**
     * Get value of the given key and reset the expire time.
     *
     * @param key      the given key
     * @param lifetime the new lifetime of key in seconds.
     * @param <T>      the value's type
     * @return the value object.
     */
    <T> T getAndExpire(String key, long lifetime);


    /**
     * The the rest lifetime of given key in seconds.
     *
     * @param key the given key
     * @return the rest lifetime in seconds.
     */
    Long getLifetime(String key);

    /**
     * Remove the given key from the cache.
     *
     * @param key the given key.
     */
    void remove(String key);



    /**
     * Increase the key's value by 1.
     *
     * @param key
     */
    Long increase(String key);


    /**
     * Decrease the key's value by 1.
     *
     * @param key
     */
    Long decrease(String key);

    /**
     * Increase the key's value by k.
     *
     * @param key
     */
    Long increaseBy(String key, int k);


    /**
     * Decrease the key's value by k.
     *
     * @param key
     */
    Long decreaseBy(String key, int k);

    /**
     * Add one or more members to a set
     * @param key
     * @param members
     * @return
     */
    Long sAdd(String key, String... members);

    /**
     * get the number of members in a set
     * @param key
     * @return
     */
    Long sCard(String key);

    /**
     * get all members in a set.
     * @param key
     * @return
     */
    Set<String> sMembers(String key);

    /**
     * subtract multiple sets
     * @param keys
     * @return
     */
    Set<String> sDiff(String... keys);

    /**
     * Add multiple sets
     * @param keys
     * @return
     */
    Set<String> sUnion(String... keys);

    /**
     * remove one or more members from a set
     * @param key
     * @param members
     * @return
     */
    Long sRem(String key, String... members);

    /**
     * determine if a given value is a member of a set
     * @param key
     * @param member
     * @return
     */
    Boolean sIsMember(String key, String member);

    /**
     * intersect multiple sets
     * @param keys
     * @return
     */
    Set<String> sInter(String... keys);

    /**
     * move a member from one set to another
     * @param sourceKey
     * @param destKey
     * @param member
     * @return
     */
    Long sMove(String sourceKey, String destKey, String member);

}
