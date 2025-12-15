package cn.magicvector.common.rest.swagger;

import cn.magicvector.common.rest.annotation.SwaggerModule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000) 
public class ApiListingReaderPlugin implements ApiListingBuilderPlugin{

	@Override
	public boolean supports(DocumentationType arg0) { 
		return true;
	}

	@Override
	public void apply(ApiListingContext apiListingContext) {  
		 ResourceGroup group = apiListingContext.getResourceGroup();   
		 Class<?> controllerClass = group.getControllerClass().get();
		 SwaggerModule sm = controllerClass.getAnnotation(SwaggerModule.class);
		 if(sm != null) {
			 apiListingContext.apiListingBuilder().description(sm.description());
		 }
		 else {
			 apiListingContext.apiListingBuilder().description("UNKNOWN");
		 } 
	}

}

