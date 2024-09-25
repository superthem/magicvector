package com.magicvector.common.rest.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.magicvector.common.rest.adapter.DateTypeAdapterFactory;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spring.web.json.Json;

import java.util.ServiceLoader;

@Configuration
public class FeignGsonConfig {

    @Bean
    public Gson gson() {
        GsonBuilder builder = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls()
                .registerTypeAdapterFactory(new DateTypeAdapterFactory())
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter());
        return builder.create();
    }

    @Bean
    public Encoder feignEncoder(Gson gson) {
        return new GsonEncoder(gson);
    }

    @Bean
    public Decoder feignDecoder(Gson gson) {
        return new GsonDecoder(gson);
    }
}