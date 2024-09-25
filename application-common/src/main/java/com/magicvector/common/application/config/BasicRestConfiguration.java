package com.magicvector.common.application.config;

import com.magicvector.common.application.filter.CORSFilter;
import com.magicvector.common.application.interceptor.CommonRequestInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class BasicRestConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CommonRequestInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public FilterRegistrationBean CORSFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CORSFilter());
        registration.addUrlPatterns("/*");
        registration.setName("cors_filter");
        registration.setOrder(Integer.MAX_VALUE-2);
        return registration;
    }
} 
