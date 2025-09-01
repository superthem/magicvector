package com.magicvector.common.application;

import com.github.tbwork.anole.loader.AnoleApp;
import com.github.tbwork.anole.loader.annotion.AnoleConfigLocation;
import com.github.tbwork.anole.loader.util.AnoleLogger;
import com.magicvector.common.basic.util.LogoUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableSwagger2
@Configuration
@ComponentScan(basePackages = {"${mv.base.package}","com.magicvector"})
@AnoleConfigLocation
@EnableFeignClients(
		basePackages = {"${mv.base.package}","com.magicvector"}
)
@EnableConfigurationProperties
public class ApplicationStarter {

	public static void main(String[] args) {

		long currentTime = System.currentTimeMillis();
		String debugAnole = System.getProperty("debugAnole");
		AnoleLogger.LogLevel defaultValue = AnoleLogger.LogLevel.INFO;
		if(debugAnole != null){
			defaultValue = AnoleLogger.LogLevel.DEBUG;
		}
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
		AnoleApp.start(ApplicationStarter.class, defaultValue);
		SpringApplication.run(ApplicationStarter.class, args);
		LogoUtil.printLogo(System.currentTimeMillis() - currentTime);

	}

}
