package com.magicvector.common.rest.config;

import com.google.gson.Gson;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientDeserializerGsonConfig {

    @Bean
    public Gson gson() {
        return new Gson();
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