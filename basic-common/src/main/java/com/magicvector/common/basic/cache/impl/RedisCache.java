package com.magicvector.common.basic.cache.impl;

import com.github.tbwork.anole.loader.Anole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RedisCache extends AbstractCache{

    @Autowired
    private JedisPool jedisPool;

    @Override
    protected void doHashSet(String hashName, String key, String value) {
        Jedis jedis =  getJedis();
        jedis.hset(hashName, key, value);
        jedis.close();
    }

    @Override
    protected void doSet(String key, String value, Long lifetime) {
        Jedis jedis =  getJedis();
        if(lifetime != null){
            jedis.setex(key, lifetime.intValue(), value);
        }
        else{
            jedis.set(key,  value);
        }
        jedis.close();
    }

    @Override
    protected void doPublish(String channel, String message) {
        Jedis jedis =  getJedis();
        jedis.publish(channel,  message);
        jedis.close();
    }

    @Override
    protected String doHashGet(String hashName, String key) {
        Jedis jedis =  getJedis();
        String result = jedis.hget(hashName, key);
        jedis.close();
        return result;
    }

    @Override
    protected long doHashDel(String hashName, String... keys) {
        Jedis jedis =  getJedis();
        Long result = jedis.hdel(hashName,keys);
        jedis.close();
        return result;
    }

    @Override
    protected Map<String, String> doHashGetAll(String hashName) {
        Jedis jedis =  getJedis();
        Map<String, String> result = jedis.hgetAll(hashName);
        jedis.close();
        return result;
    }

    @Override
    protected Object doGet(String key, Long lifetime) {
        Jedis jedis =  getJedis();
        String result = jedis.get(key);;
        if(lifetime != null){
          jedis.expire(key, lifetime.intValue());
        }
        jedis.close();
        return result;
    }

    @Override
    protected boolean isLocal() {
        return false;
    }


    @Override
    public Long getLifetime(String key) {
        Jedis jedis =  getJedis();
        Long result = jedis.ttl(key);
        jedis.close();
        return result;
    }

    @Override
    public void remove(String key) {
        Jedis jedis =  getJedis();
        jedis.del(key);
        jedis.close();
    }

    @Override
    public long hsize(String hkey) {
        Jedis jedis =  getJedis();
        long result = jedis.hlen(hkey);
        jedis.close();
        return result;
    }

    @Override
    public Long increase(String key) {
        Jedis jedis =  getJedis();
        Long res=jedis.incr(key);
        jedis.close();
        return res;
    }

    @Override
    public Long decrease(String key) {
        Jedis jedis =  getJedis();
        Long res=jedis.decr(key);
        jedis.close();
        return res;
    }

    @Override
    public Long increaseBy(String key, int k) {
        Jedis jedis =  getJedis();
        Long res=jedis.incrBy(key, k);
        jedis.close();
        return res;
    }

    @Override
    public Long decreaseBy(String key, int k) {
        Jedis jedis =  getJedis();
        Long res=jedis.decrBy(key, k);
        jedis.close();
        return res;
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
        Jedis jedis =  getJedis();
        Long res=jedis.sadd(key, members);
        jedis.close();
        return res;
    }

    /**
     * get the number of members in a set
     *
     * @param key
     * @return
     */
    @Override
    public Long sCard(String key) {
        Jedis jedis =  getJedis();
        Long res=jedis.scard(key);
        jedis.close();
        return res;
    }

    /**
     * get all members in a set.
     *
     * @param key
     * @return
     */
    @Override
    public Set<String> sMembers(String key) {
        Jedis jedis =  getJedis();
        Set<String> res=jedis.smembers(key);
        jedis.close();
        return res;
    }

    /**
     * subtract multiple sets
     *
     * @param keys
     * @return
     */
    @Override
    public Set<String> sDiff(String... keys) {
        Jedis jedis =  getJedis();
        Set<String> res=jedis.sdiff(keys);
        jedis.close();
        return res;
    }

    /**
     * Add multiple sets
     *
     * @param keys
     * @return
     */
    @Override
    public Set<String> sUnion(String... keys) {
        Jedis jedis =  getJedis();
        Set<String> res=jedis.sunion(keys);
        jedis.close();
        return res;
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
        Jedis jedis =  getJedis();
        Long res=jedis.srem(key, members);
        jedis.close();
        return res;
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
        Jedis jedis =  getJedis();
        Boolean res=jedis.sismember(key, member);
        jedis.close();
        return res;
    }

    /**
     * intersect multiple sets
     *
     * @param keys
     * @return
     */
    @Override
    public Set<String> sInter(String... keys) {
        Jedis jedis =  getJedis();
        Set<String> res=jedis.sinter(keys);
        jedis.close();
        return res;
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
        Jedis jedis =  getJedis();
        Long res=jedis.smove(sourceKey, destKey, member);
        jedis.close();
        return res;
    }

    @Override
    public List<String> hmget(String key, String... fields){
        Jedis jedis = getJedis();
        List<String> res = jedis.hmget(key,fields);
        jedis.close();
        return res;
    }





    private Jedis getJedis(){
        return  jedisPool.getResource();
    }

}
