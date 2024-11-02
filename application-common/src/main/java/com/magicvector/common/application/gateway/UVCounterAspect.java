package com.magicvector.common.application.gateway;

import com.google.common.collect.Lists;
import com.magicvector.common.application.counter.CouterService;
import com.magicvector.common.basic.cache.Cache;
import com.magicvector.common.basic.util.DateUtil;
import com.magicvector.common.basic.util.IpUtil;
import com.magicvector.common.rest.annotation.UVCounter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

/**
 * UV计数器
 */
@Aspect
@Component
@Order(30)
@Slf4j
public class UVCounterAspect extends AbstractAspect{

    @Pointcut("@annotation(com.magicvector.common.rest.annotation.UVCounter)")
    public void countPointcut() {
    }
    @Autowired
    private CouterService couterService;

    @After("countPointcut()")
    public void countPVAndUV(JoinPoint joinPoint) {

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = IpUtil.getIp(httpServletRequest);
        // 获取当前执行的方法
        Method method = getMethodFromJoinPoint(joinPoint);

        // 检查方法上是否存在 MyAnnotation 注解
        if (method.isAnnotationPresent(UVCounter.class)) {
            // 获取注解
            UVCounter annotation = method.getAnnotation(UVCounter.class);
            // 获取注解的值
            String page = annotation.page();
            couterService.counter("UV", page, ip);
        }

    }


}
