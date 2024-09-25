package com.magicvector.common.basic.config;

import com.github.tbwork.anole.loader.Anole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        //替换通用的redis
        String redisUrl = Anole.getProperty("spring.redis.host");
        Integer redisPort = Anole.getIntProperty("spring.redis.port");
        Integer redisMaxTotal = Anole.getIntProperty("redis.pool.max.total");
        Integer redisMaxIdle = Anole.getIntProperty("redis.pool.max.idle");
        String redisPassword = Anole.getProperty("spring.redis.password");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisMaxTotal);
        poolConfig.setMaxIdle(redisMaxIdle);
        poolConfig.setMaxWaitMillis(1000 * 10);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnCreate(false);

        return new JedisPool(poolConfig, redisUrl, redisPort, 3000, redisPassword);
    }
}
