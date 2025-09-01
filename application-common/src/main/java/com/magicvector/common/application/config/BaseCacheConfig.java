package com.magicvector.common.application.config;

import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.basic.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseCacheConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean("baseCache")
    public Cache baseCache() {
        String cacheBeanName = Anole.getProperty("session.cache.beanName", "redisCache");
        return applicationContext.getBean(cacheBeanName, Cache.class);
    }

}
