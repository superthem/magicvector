package cn.magicvector.common.basic.locks;

public interface DistLock {
    /**
     * 尝试获取指定资源的锁，立即返回。
     * @param resourceId 需要加锁的资源ID。
     * @return 成功获取锁时返回锁标志（UUID），失败时返回 null。
     */
    String lock(String resourceId);


    /**
     * 尝试在指定的时间内获取指定资源的锁。
     * @param resourceId 需要加锁的资源ID。
     * @param wait 等待锁的超时时间（毫秒）。
     * @return 成功获取锁时返回锁标志（UUID），失败时返回 null。
     */
    String lock(String resourceId, long wait);

    /**
     * 尝试在指定的时间内获取指定资源的锁。
     * @param resourceId 需要加锁的资源ID。
     * @param wait 等待锁的超时时间（毫秒）。
     * @param expire 获得锁后，锁自动释放的时间。
     * @return 成功获取锁时返回锁标志（UUID），失败时返回 null。
     */
    String lock(String resourceId, long wait, long expire);

    /**
     * 释放锁，只有持有锁的客户端才能成功释放锁。
     * @param resourceId 需要解锁的资源ID。
     * @param lockValue 锁标志（加锁时生成的 UUID）。
     * @return 解锁成功返回 true，失败返回 false。
     */
    boolean unlock(String resourceId, String lockValue);

    /**
     * 查询指定资源是否被锁定。
     * @param resourceId 需要查询的资源ID。
     * @return 如果锁被占用则返回 true，反之返回 false。
     */
    boolean isLocked(String resourceId);
}
