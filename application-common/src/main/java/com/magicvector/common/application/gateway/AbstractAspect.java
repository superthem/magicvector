package com.magicvector.common.application.gateway;


import com.magicvector.common.basic.errors.Error;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Method;

public abstract class AbstractAspect {


    // 从 JoinPoint 获取执行的方法对象
    protected Method getMethodFromJoinPoint(JoinPoint joinPoint) {
        try{
            // 获取目标类
            Class<?> targetClass = joinPoint.getTarget().getClass();
            // 获取方法名
            String methodName = joinPoint.getSignature().getName();
            // 获取方法参数类型
            Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes();
            // 返回方法对象
            return targetClass.getMethod(methodName, parameterTypes);
        }
        catch (Exception e){
            throw  new MagicException(Errors.UNKNOWN_ERROR, e.getMessage());
        }
    }
}
