package com.magicvector.common.basic.config;

import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
@ConditionalOnProperty(name = "mv.mongodb.enabled", havingValue = "true")
public class MongoConfig {

    @Value("${mv.mongodb.uri}")
    private String mongoDbUri;


    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoDbUri);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        // 去掉参数部分，避免 `?` 干扰
        String uriWithoutParams = mongoDbUri.contains("?")
                ? mongoDbUri.substring(0, mongoDbUri.indexOf("?"))
                : mongoDbUri;

        // 检查是否包含数据库名
        int lastSlashIndex = uriWithoutParams.lastIndexOf("/");
        if (lastSlashIndex <= "mongodb://".length() || lastSlashIndex == uriWithoutParams.length() - 1) {
            throw new MagicException(Errors.LOGIC_ERROR,"MongoDB URI 必须包含数据库名，例如：mongodb://localhost:27017/mydb");
        }

        return new SimpleMongoClientDatabaseFactory(mongoDbUri);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}