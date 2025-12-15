package cn.magicvector.common.rpc.feign;

import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author shawn feng
 * @description
 * @date 2021/12/27 16:35
 */
@Component
public class FeignClientBeanFactoryPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware, Ordered {

    private Environment environment;
    private final String PLACE_HOLDER = "ENV";
    private final String ANOLE_ENV = "anole.env";
    private final String FEIGN_NAME = "name";
    private final String FEIGN_URL = "url";
    private final String LOCAL = "local";

    @SneakyThrows
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String feignClientFactoryBeanName = "org.springframework.cloud.openfeign.FeignClientFactoryBean";
        Class<?> beanNameClz = Class.forName(feignClientFactoryBeanName);
        String[] beanNames = beanFactory.getBeanNamesForType(beanNameClz);
        for (String beanName : beanNames) {
            Object factoryBean = beanFactory.getBean(beanName); //factoryBean在postProcessor前就已经创建了，直接修改
            processFeignClientProperty(factoryBean, beanNameClz);
        }
    }

    private void processFeignClientProperty(Object factoryBean, Class<?> clazz) {
        String env = environment.getProperty(ANOLE_ENV);
        String name = (String) getProperty(clazz, FEIGN_NAME, factoryBean);
        if (name.contains(PLACE_HOLDER)) {
            String finalName = name.replace(PLACE_HOLDER, env.toUpperCase());
            setProperty(clazz, FEIGN_NAME, finalName, factoryBean);
        }
        if (!LOCAL.equalsIgnoreCase(env) && name.endsWith("SERVICE")) {
            setProperty(clazz, FEIGN_URL, "", factoryBean);
        }
    }

    @SneakyThrows
    private Object getProperty(Class clazz, String fieldName, Object obj) {
        Field field = ReflectionUtils.findField(clazz, fieldName);
        ReflectionUtils.makeAccessible(field);
        return field.get(obj);
    }

    @SneakyThrows
    private void setProperty(Class clazz, String fieldName, String newValue, Object obj) {
        Field field = ReflectionUtils.findField(clazz, fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, obj, newValue);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
