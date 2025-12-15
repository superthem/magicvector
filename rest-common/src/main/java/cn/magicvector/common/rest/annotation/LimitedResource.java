package cn.magicvector.common.rest.annotation;

import java.lang.annotation.*;

/**
 * @author Tommy.Tesla
 * @description
 * @date 2022/9/9 09:22
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LimitedResource {

    /**
     * 请求在窗口时间内独占的资源的唯一标识
     */
    String resource();

    /**
     * 窗口时间长度，单位毫秒，默认为1000ms
     */
    long window() default 1000;

}
