package cn.magicvector.common.rest.annotation;

import java.lang.annotation.*;

/**
 *
 * @author yangheng
 * @date 2021/5/18 11:48
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 需要锁定的资源ID
     */
    String resource() default "";

    /**
     * 锁定成功后，最长时间。单位为毫秒ms
     */
    long expire() default 3000L;

    /**
     * 等待的超时时间，超过了就不再尝试获取锁。单位为毫秒ms
     */
    long timeout() default 0L;

    /**
     * 锁获取失败后的错误信息。
     */
    String message() default "获取分布式锁失败！";
}
