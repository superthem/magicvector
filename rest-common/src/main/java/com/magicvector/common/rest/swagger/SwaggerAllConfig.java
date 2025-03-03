package com.magicvector.common.rest.swagger;

import com.github.tbwork.anole.loader.Anole;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;

@Configuration
public class SwaggerAllConfig {

	@Bean public Docket allInOne() {
	     return new Docket(DocumentationType.SWAGGER_2)
	    		    .groupName("全部接口清单")
	                .apiInfo(apiInfo())  
	                .select() 
                       .apis(RequestHandlerSelectors.basePackage(Anole.getProperty("swagger.scan.base.package")))
                       .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                       .build()
	                ;
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("魔法向量", "https://www.superthem.com", "magicvectercorp@163.com");
        return new ApiInfoBuilder()
                .title("MagicVector Swagger Production")
                .description("接口汇总，查看具体模块请在右上角选择。")
                .contact(contact)
                .version(Anole.getProperty("version"))
                .build();
    }

    @Bean
    UiConfiguration uiConfig() {
      return UiConfigurationBuilder.builder()
          .deepLinking(true)
          .displayOperationId(false)
          .defaultModelsExpandDepth(1)
          .defaultModelExpandDepth(1)
          .defaultModelRendering(ModelRendering.EXAMPLE)
          .displayRequestDuration(false)
          .docExpansion(DocExpansion.NONE)
          .filter(false)
          .maxDisplayedTags(null)
          .operationsSorter(OperationsSorter.ALPHA)
          .showExtensions(false)
          .tagsSorter(TagsSorter.ALPHA)
          .validatorUrl(null)
          .build();
    }

}
