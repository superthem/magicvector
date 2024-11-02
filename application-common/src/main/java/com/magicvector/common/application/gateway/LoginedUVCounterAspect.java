package com.magicvector.common.application.gateway;

import com.magicvector.common.application.counter.CouterService;
import com.magicvector.common.basic.context.GlobalContext;
import com.magicvector.common.basic.util.IpUtil;
import com.magicvector.common.basic.util.S;
import com.magicvector.common.rest.annotation.LoginedUVCounter;
import com.magicvector.common.rest.annotation.UVCounter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * UV计数器
 */
@Aspect
@Component
@Order(30)
@Slf4j
public class LoginedUVCounterAspect extends AbstractAspect{

    @Pointcut("@annotation(com.magicvector.common.rest.annotation.LoginedUVCounter)")
    public void countPointcut() {
    }
    @Autowired
    private CouterService couterService;

    @After("countPointcut()")
    public void countPVAndUV(JoinPoint joinPoint) {

        // 获取当前执行的方法
        Method method = getMethodFromJoinPoint(joinPoint);
        // 检查方法上是否存在 MyAnnotation 注解
        if (method.isAnnotationPresent(LoginedUVCounter.class)) {
            // 获取注解
            LoginedUVCounter annotation = method.getAnnotation(LoginedUVCounter.class);
            // 获取注解的值
            String page = annotation.page();

            String userid = null;
            if(GlobalContext.getContext() != null
                    && GlobalContext.getContext().getUser() != null
            ){
                userid = GlobalContext.getContext().getUser().getUserId();
            }

            couterService.counter("UV", page, userid);
        }

    }


}
