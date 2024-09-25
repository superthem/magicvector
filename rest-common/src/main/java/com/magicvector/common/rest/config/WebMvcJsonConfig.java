package com.magicvector.common.rest.config;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.magicvector.common.rest.adapter.DateTypeAdapterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.spring.web.json.Json;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author zhangg
 */
@Configuration
@EnableWebMvc
public class WebMvcJsonConfig implements WebMvcConfigurer {

    @Autowired
    private Gson gson; // 注入已有的Gson Bean

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(gsonHttpMessageConverterBean());
        converters.add(stringHttpMessageConverter());
    }

    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverterBean() {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson); // 使用注入的Gson实例
        return converter;
    }

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        // 用于prometheus 推数据
        StringHttpMessageConverter converter = new StringHttpMessageConverter();
        converter.setSupportedMediaTypes(Lists.newArrayList(MediaType.TEXT_PLAIN));
        return converter;
    }
}