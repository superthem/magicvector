package com.magicvector.common.service;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.tbwork.anole.loader.AnoleApp;
import com.github.tbwork.anole.loader.annotion.AnoleConfigLocation;
import com.github.tbwork.anole.loader.util.AnoleLogger;
import com.magicvector.common.basic.util.LogoUtil;
import com.magicvector.common.rpc.config.ClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.TimeZone;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"${magic.vector.base.package}", "com.magicvector"})
@EnableSwagger2
@ComponentScan(basePackages = {"${magic.vector.base.package}", "com.magicvector"})
@AnoleConfigLocation
public class ServiceStarter {

	public static void main(String[] args) {

		long currentTime = System.currentTimeMillis();
		String debugAnole = System.getProperty("debugAnole");
		AnoleLogger.LogLevel defaultValue = AnoleLogger.LogLevel.INFO;
		if(debugAnole != null){
			defaultValue = AnoleLogger.LogLevel.DEBUG;
		}
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
		AnoleApp.start(ServiceStarter.class, defaultValue);
		SpringApplication.run(ServiceStarter.class, args);
		LogoUtil.printLogo(System.currentTimeMillis() - currentTime);
	}
}
