package com.magicvector.common.basic.config;

import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.util.Asserts;
import com.magicvector.common.basic.util.S;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
@Configuration
public class RedisConfig {


    @Bean
    public JedisPool jedisPool() {
        //替换通用的redis
        String redisUrl = Anole.getProperty("magic.vector.redis.host");
        Asserts.assertTrue(S.isNotEmpty(redisUrl), Errors.CONFIG_NOT_COMPLETE,"未指定Redis地址，请通过magic.vector.redis.host指定一个redis地址。");
        int redisPort = Anole.getIntProperty("magic.vector.redis.port", 6379);
        int redisMaxTotal = Anole.getIntProperty("magic.vector.redis.pool.max.total", 50);
        int redisMaxIdle = Anole.getIntProperty("magic.vector.redis.pool.max.idle", 20);
        String redisPassword = Anole.getProperty("magic.vector.redis.password");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisMaxTotal);
        poolConfig.setMaxIdle(redisMaxIdle);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnCreate(true);

        return new JedisPool(poolConfig, redisUrl, redisPort, 3000, redisPassword);
    }

}
