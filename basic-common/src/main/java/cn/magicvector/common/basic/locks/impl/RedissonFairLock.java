package cn.magicvector.common.basic.locks.impl;

import cn.magicvector.common.basic.locks.DistLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(name = "mv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonFairLock implements DistLock {

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
        RLock fairLock =redissonClient.getFairLock(lockKey);

        try {
            // tryLock(waitTime, leaseTime, unit)
            // - waitTime: 最多等待多久拿到锁
            // - leaseTime: 锁自动释放时间（过期时间）
            // - 如果没拿到锁，返回 false
            boolean acquired = fairLock.tryLock(wait, expire, TimeUnit.MILLISECONDS);

            if (acquired) {
                log.info("Redisson lock acquired for resource: {}", resourceId);
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
            // 注意：Redisson 的 unlock() 不需要传 lockValue
            // 它内部通过 ThreadLocal 和 UUID 自动识别是否是持有者
            // 如果你传了错误的 lockValue，说明上层逻辑有问题
            // 这里我们假设 lockValue 是有效的，只做释放
            lock.unlock();
            log.info("Redisson lock released for resource: {}", resourceId);
            return true;
        } catch (IllegalMonitorStateException e) {
            // 当前线程不是锁的持有者
            log.warn("Attempt to unlock non-held lock for resource: {}", resourceId);
            return false;
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