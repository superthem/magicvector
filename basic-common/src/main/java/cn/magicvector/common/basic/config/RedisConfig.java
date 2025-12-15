package cn.magicvector.common.basic.config;

import com.github.tbwork.anole.loader.Anole;
import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.util.Asserts;
import cn.magicvector.common.basic.util.S;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
@Configuration
@ConditionalOnProperty(name = "mv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Bean
    public JedisPool jedisPool() {

        String redisUrl = Anole.getProperty("mv.redis.host");
        Asserts.assertTrue( S.isNotEmpty(redisUrl), Errors.CONFIG_NOT_COMPLETE,"未指定Redis地址，请通过mv.redis.host指定一个redis地址。");
        int redisPort = Anole.getIntProperty("mv.redis.port", 6379);
        int redisMaxTotal = Anole.getIntProperty("mv.redis.pool.max.total", 50);
        int redisMaxIdle = Anole.getIntProperty("mv.redis.pool.max.idle", 20);
        String redisPassword = Anole.getProperty("mv.redis.password");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisMaxTotal);
        poolConfig.setMaxIdle(redisMaxIdle);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnCreate(true);

        return new JedisPool(poolConfig, redisUrl, redisPort, 3000, redisPassword);
    }

}
