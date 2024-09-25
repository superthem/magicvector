package com.magicvector.common.application.gateway;

import com.google.common.collect.Lists;
import com.magicvector.common.basic.cache.Cache;
import com.magicvector.common.basic.util.DateUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

/**
 * 通用后端计数器
 */
@Aspect
@Component
@Order(30)
@Slf4j
public class CounterAspect {

    @Autowired
    @Qualifier("redisDataCache")
    private Cache cache;

    @Pointcut("@annotation(com.magicvector.common.basic.annotation.VisitCount)")
    public void countPointcut() {
    }

    @After("countPointcut()")
    public void countPVAndUV(JoinPoint joinPoint) {

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 获取接口名称
        String interfaceName = joinPoint.getSignature().getName();
        String type = "APP";
        String platform = httpServletRequest.getParameter("_platform");
        String client_id = httpServletRequest.getParameter("_client_id");
        //只记录登陆用户
        if (StringUtils.isEmpty(client_id)) {
            return;
        }
        if (!StringUtils.isEmpty(platform) && platform.equals("web")) {
            type = "WEB";
        }
        String date = DateUtil.toString(new Date());
        String pvKey = type + ":PV:" + date;
        String pvInterfaceKey = type + ":PV:" + date + ":" + interfaceName;

        //获取pv数组
        String clientIds = (String) Optional.ofNullable(cache.get(pvKey)).orElse("");
        ArrayList<String> clientIdList = Lists.newArrayList(clientIds.split(","));
        clientIdList.add(client_id);
        String pageClientIds = (String) Optional.ofNullable(cache.get(pvInterfaceKey)).orElse("");
        ArrayList<String> pageClientIdList = Lists.newArrayList(pageClientIds.split(","));
        pageClientIdList.add(client_id);
        cache.set(pvKey, String.join(",", clientIdList), 2 * 24 * 3600);
        cache.set(pvInterfaceKey, String.join(",", pageClientIdList), 2 * 24 * 3600);
    }


}
