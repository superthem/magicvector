package com.magicvector.common.application.gateway;

import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.locks.DistLock;
import com.magicvector.common.basic.util.S;
import com.magicvector.common.rest.annotation.LimitedResource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

/**
 * @author shawn feng
 * @description
 * @date 2022/9/9 09:24
 */
@Aspect
@Component
@Order(20)
@Slf4j
@ConditionalOnProperty(name = "mv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RepeatRequestBlockAspect {

    @Autowired
    @Qualifier("redissonFairLock")
    private DistLock distLock;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Pointcut("@annotation(com.magicvector.common.rest.annotation.LimitedResource)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = point.getTarget().getClass().getName();
        Object[] args = point.getArgs();
        String[] paramNames = signature.getParameterNames();
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; ++i) {
            context.setVariable(paramNames[i], args[i]);
        }
        LimitedResource limitedResource = method.getAnnotation(LimitedResource.class);
        long windowTimeSize = limitedResource.window();

        String resource;
        try {
            resource = S.isEmpty(limitedResource.resource()) ? method.getName() : this.parser.parseExpression(limitedResource.resource()).getValue(context, String.class);
        } catch (Exception e) {
            throw new MagicException(Errors.LOCK_RESOURCE_PARSE_ERROR, e.getMessage());
        }
        String lockTarget =  "LimitedResourceLock:" + resource;

        String lock = distLock.lock(lockTarget, 0 ,windowTimeSize);

        if(lock == null){
            log.warn("限制性资源目标正在被访问中, class={}, method={}, resource={}", className, method, resource);
            throw new MagicException(Errors.RESOURCE_LIMITED_ERROR, "此接口为限制性资源接口，请勿重复访问。");
        }

        log.debug("已获得限制性资源的访问权限,class={},method={},resourceId={}", className, method.getName(), resource);
        try {
            return point.proceed();
        } finally {
            // 无论执行过程中是否抛出异常，最后都要释放锁
            distLock.unlock(resource, lock);
        }
    }
}
