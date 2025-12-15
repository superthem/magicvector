package cn.magicvector.common.rpc.config;

import cn.magicvector.common.rpc.feign.FeignClientBeanFactoryPostProcessor;
import cn.magicvector.common.rpc.filter.FeignClientFilter;
import cn.magicvector.common.rpc.filter.FeignServerFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
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
