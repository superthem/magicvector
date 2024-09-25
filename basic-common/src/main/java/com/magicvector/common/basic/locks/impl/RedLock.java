package com.magicvector.common.basic.locks.impl;
import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.basic.locks.DistLock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
@Slf4j
public class RedLock implements DistLock{

    @Autowired
    private JedisPool jedisPool;
    private static final String LOCK_PREFIX = "REDLOCK:";
    @Value("${magic.vector.lock.red-lock.maxRetries:20}")
    private int maxRetries;
    @Value("${magic.vector.lock.red-lock.expireTime.default:30000}")
    private int defaultExpireTime;

    @Override
    public String lock(String resourceId) {
        return lock(resourceId, 0, defaultExpireTime);
    }

    @Override
    public String lock(String resourceId, long wait) {
        return lock(resourceId, wait, defaultExpireTime);
    }

    @Override
    public String lock(String resourceId, long wait, long expire) {
        String lockKey = LOCK_PREFIX + resourceId;
        String lockValue = UUID.randomUUID().toString();
        long endTime = System.currentTimeMillis() + wait;

        try (Jedis jedis = jedisPool.getResource()) {
            SetParams setParams = new SetParams().nx().px(expire);
            while (System.currentTimeMillis() < endTime) {
                String result = jedis.set(lockKey, lockValue, setParams);
                if ("OK".equals(result)) {
                    log.info("Lock acquired for resource: {}", resourceId);
                    return lockValue;
                }
                try {
                    Thread.sleep(50); // 重试前等待
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted while waiting for lock on resource: {}", resourceId);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("Error acquiring lock for resource: {}, error: {}", resourceId, e.getMessage());
        }

        return null; // 超时未获取到锁
    }

    @Override
    public boolean unlock(String resourceId, String lockValue) {
        String lockKey = LOCK_PREFIX + resourceId;

        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else return 0 end";

        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(luaScript, 1, lockKey, lockValue);
            if ("1".equals(result.toString())) {
                log.info("Lock released for resource: {}", resourceId);
                return true;
            } else {
                log.warn("Failed to release lock or lock already released for resource: {}", resourceId);
            }
        } catch (Exception e) {
            log.error("Error releasing lock for resource: {}, error: {}", resourceId, e.getMessage());
        }

        return false;
    }

    @Override
    public boolean isLocked(String resourceId) {
        String lockKey = LOCK_PREFIX + resourceId;

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(lockKey);
        } catch (Exception e) {
            log.error("Error checking lock status for resource: {}, error: {}", resourceId, e.getMessage());
        }

        return false;
    }

}