package com.magicvector.common.application.gateway;

import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.locks.DistLock;
import com.magicvector.common.basic.util.S;
import com.magicvector.common.rest.annotation.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

/**
 * @author yangheng
 * @date 2021/5/18 14:05
 */
@Aspect()
@Order(100)
@Component
@Slf4j
public class DistLockAspect {
    private ExpressionParser parser = new SpelExpressionParser();
    @Autowired
    private DistLock distLock;

    @Pointcut("@annotation(com.magicvector.common.rest.annotation.DistributedLock)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        String className = point.getTarget().getClass().getName();
        Object[] args = point.getArgs();
        String[] paramNames = signature.getParameterNames();
        EvaluationContext context = new StandardEvaluationContext();
        for(int i = 0; i < args.length; ++i) {
            context.setVariable(paramNames[i], args[i]);
        }
        DistributedLock lockPoint = method.getAnnotation(DistributedLock.class);
        long expire =  lockPoint.expire();
        long waitTime = lockPoint.timeout();
        String resource;
        try {
            resource = S.isEmpty(lockPoint.resource()) ? method.getName() : this.parser.parseExpression(lockPoint.resource()).getValue(context, String.class);
        } catch (Exception e) {
            log.error("SpEL表达式解析失败，使用默认方法名作为资源标识", e);
            resource = method.getName();  // 使用默认的methodName作为resource
        }
        String lockTarget =  "DistributedLock:" + resource;
        String lock = this.distLock.lock(lockTarget, expire, waitTime);
        if (lock == null) {
            log.warn("获取分布式锁失败,class={},method={},resourceId={}", className, method.getName(), resource);
            throw new MagicException(Errors.DISTRIBUTED_LOCK_TIMEOUT_ERROR, lockPoint.message());
        }

        log.debug("获取分布式锁成功,class={},method={},resourceId={}", className, method.getName(), resource);
        try {
            return point.proceed();
        } finally {
            // 无论执行过程中是否抛出异常，最后都要释放锁
            distLock.unlock(resource, lock);
        }
    }
}
