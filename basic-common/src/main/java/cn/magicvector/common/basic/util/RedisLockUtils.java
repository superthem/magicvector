package cn.magicvector.common.basic.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "mv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisLockUtils {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey   锁的键
     * @param requestId 请求标识（用于释放锁时验证）
     * @param expireTime 锁的过期时间（单位：秒）
     * @return 是否成功获取锁
     */
    public boolean setLock(String lockKey, String requestId, long expireTime) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 使用 SET 命令，设置锁的键和值，并设置过期时间
            SetParams setParams = SetParams.setParams().nx().ex(expireTime);
            String result = jedis.set(lockKey, requestId, setParams);
            return "OK".equals(result);
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁的键
     * @param requestId 请求标识（用于验证锁的所有者）
     * @return 是否成功释放锁
     */
    public boolean releaseLock(String lockKey, String requestId) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 获取锁的当前值
            String currentValue = jedis.get(lockKey);
            if (currentValue != null && currentValue.equals(requestId)) {
                // 如果当前值匹配请求标识，则删除锁
                jedis.del(lockKey);
                return true;
            }
            return false;
        }
    }

    /**
     * 释放分布式锁（简化版，不验证请求标识）
     *
     * @param lockKey 锁的键
     * @return 是否成功释放锁
     */
    public boolean releaseLock(String lockKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 直接删除锁
            return jedis.del(lockKey) > 0;
        }
    }
}