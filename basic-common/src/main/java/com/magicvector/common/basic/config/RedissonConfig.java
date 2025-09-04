package com.magicvector.common.basic.config;

import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.basic.util.Asserts;
import com.magicvector.common.basic.util.S;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "mv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() throws IOException {
        // 是否启用 Redis
        String redisHost = Anole.getProperty("mv.redis.host");
        Asserts.assertTrue(S.isNotEmpty(redisHost), "未指定Redis地址，请通过 mv.redis.host 配置");
        // 读取配置
        int redisPort = Anole.getIntProperty("mv.redis.port", 6379);
        String password = Anole.getProperty("mv.redis.password"); // 可为空
        int database = Anole.getIntProperty("mv.redis.database", 0);
        int connectionPoolSize = Anole.getIntProperty("mv.redisson.pool.size", 50);
        int connectionMinimumIdleSize = Anole.getIntProperty("mv.redisson.pool.idle", 20);
        int timeout = Anole.getIntProperty("mv.redisson.timeout", 3000); // 毫秒

        // 构建 Redisson 配置
        Config config = new Config();
        String redisAddress = "redis://" + redisHost + ":" + redisPort;

        config.useSingleServer()
                .setAddress(redisAddress)
                .setPassword(password)
                .setDatabase(database)
                .setTimeout(timeout)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize);

        // 可选：设置 codec，例如使用 JSON 序列化
        // config.setCodec(new JsonJacksonCodec());

        // 创建并返回 RedissonClient
        return Redisson.create(config);


    }
}
