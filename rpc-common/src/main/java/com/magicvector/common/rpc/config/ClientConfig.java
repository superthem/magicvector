package com.magicvector.common.rpc.config;

import com.magicvector.common.rpc.feign.FeignClientBeanFactoryPostProcessor;
import com.magicvector.common.rpc.filter.FeignClientFilter;
import com.magicvector.common.rpc.filter.FeignServerFilter;
import feign.RequestInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean("myInterceptor")
    public RequestInterceptor getRequestInterceptor() {
        return new FeignClientFilter();
    }


    @Bean
    public FilterRegistrationBean feignServerFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new FeignServerFilter());
        registration.addUrlPatterns("/*");
        registration.setName("FeignServerFilter");
        registration.setOrder(Integer.MAX_VALUE-1);
        return registration;
    }

}
