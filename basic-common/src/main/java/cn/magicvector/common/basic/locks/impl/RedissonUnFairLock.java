package cn.magicvector.common.basic.locks.impl;

import cn.magicvector.common.basic.locks.DistLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "mv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonUnFairLock implements DistLock {

    @Autowired
    private RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "DISTLOCK:";

    @Override
    public String lock(String resourceId) {
        // 等同于 tryLock(0, defaultExpire)，即不等待，立即返回
        return lock(resourceId, 0, 30000); // 默认30秒过期
    }

    @Override
    public String lock(String resourceId, long wait) {
        // 不指定 expire，使用默认值
        return lock(resourceId, wait, 30000); // 默认30秒过期
    }

    @Override
    public String lock(String resourceId, long wait, long expire) {
        String lockKey = LOCK_PREFIX + resourceId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // tryLock(waitTime, leaseTime, unit)
            // - waitTime: 最多等待多久拿到锁
            // - leaseTime: 锁自动释放时间（过期时间）
            // - 如果没拿到锁，返回 false
            boolean acquired = lock.tryLock(wait, expire, TimeUnit.MILLISECONDS);

            if (acquired) {
                log.debug("Redisson lock acquired for resource: {}", resourceId);
                // 返回一个唯一标识（虽然 Redisson 不需要你传，但接口要求返回 UUID）
                // 注意：这里返回的 UUID 并不是 Redisson 内部用的，仅用于满足接口
                return UUID.randomUUID().toString();
            } else {
                log.warn("Failed to acquire Redisson lock for resource: {} within {}ms", resourceId, wait);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted while waiting for lock on resource: {}", resourceId);
            return null;
        }
    }

    @Override
    public boolean unlock(String resourceId, String lockValue) {
        String lockKey = LOCK_PREFIX + resourceId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.isHeldByCurrentThread()) {
                log.warn("Attempt to unlock non-held lock for resource: {}", resourceId);
                return false;
            }
            lock.unlock();
            log.debug("Redisson lock released for resource: {}", resourceId);
            return true;
        } catch (Exception e) {
            log.error("Error releasing Redisson lock for resource: {}", resourceId, e);
            return false;
        }
    }

    @Override
    public boolean isLocked(String resourceId) {
        String lockKey = LOCK_PREFIX + resourceId;
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
}