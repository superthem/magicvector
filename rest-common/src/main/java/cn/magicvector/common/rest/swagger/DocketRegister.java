package cn.magicvector.common.rest.swagger;

import com.github.tbwork.anole.loader.Anole;
import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.util.Asserts;
import cn.magicvector.common.basic.util.S;
import cn.magicvector.common.rest.annotation.SwaggerModule;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DocketRegister implements BeanDefinitionRegistryPostProcessor{
 
	private  Set<String> groupSet = new HashSet<String>();
	private  List<DocketInfo> docketInfos = new ArrayList<DocketInfo>();  
	private  static DocumentationType dt = DocumentationType.SWAGGER_2;
	
	private void collect() {
		List<Class<?>> docketClasses = getClassWithSpecifiedAnnotation(SwaggerModule.class);
		for (Class<?> klass : docketClasses) {
			SwaggerModule annotation = klass.getAnnotation(SwaggerModule.class);
			addDocket(annotation.name(), annotation.description(),
					  annotation.version(), annotation.author(), annotation.email(), klass);
		}
	}
	
	private String getValue(String valueName) {
		if(valueName.startsWith("${") && valueName.endsWith("}")) {
			valueName = valueName.replace("${", "").replace("}", "");
			valueName = Anole.getProperty(valueName);
		}
		return valueName;
	}
	
	private List<Class<?>> getClassWithSpecifiedAnnotation(Class<? extends Annotation> annotation){
		List<Class<?>> result = new ArrayList<Class<?>>();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
		String scanBasePackage = Anole.getProperty("swagger.scan.base.package");
		Asserts.assertTrue(S.isNotEmpty(scanBasePackage), "请设置swagger.scan.base.package为你的controller所在包");
        for (BeanDefinition bd : scanner.findCandidateComponents(Anole.getProperty("swagger.scan.base.package"))) {
        	try {
				result.add(Class.forName(bd.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		String builtinBasePackage = Anole.getProperty("mv.builtin.controller.base.package");
		if(S.isNotEmpty(builtinBasePackage)){
			for (BeanDefinition bd : scanner.findCandidateComponents(builtinBasePackage)) {
				try {
					result.add(Class.forName(bd.getBeanClassName()));
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}
	private  void addDocket(String groupName, String description, String version, String author, String email, Class<?> targetClass) {
		groupName = getValue(groupName);
		description = getValue(description);
		version = getValue(version);
		author = getValue(author);
		email = getValue(email);
		Asserts.assertTrue(S.isNotEmpty(groupName), Errors.LOGIC_ERROR,"Group name can not be empty or null!");
		Asserts.assertTrue(!groupSet.contains(groupName), Errors.LOGIC_ERROR,"Duplicate group names for Dockets ("+groupName+")!");
		ApiInfo info = getApiInfo(groupName, description, version, author, email);
		DocketInfo docketInfo = createDocket(groupName, info, targetClass);
		docketInfos.add(docketInfo);
		groupSet.add(groupName);
	}
	
	private  DocketInfo createDocket(String groupName, ApiInfo apiInfo, Class<?> targetClass) {
		DocketInfo dw = new DocketInfo();
		dw.setApiInfo(apiInfo);
		dw.targetClass = targetClass;
		dw.setGroupName(groupName);
		return dw;
    }


	private static Tag [] copyArray(Tag [] target, int start, int end) {
		    Tag [] result = new Tag[end - start];
			for(int i = start ; i < end; i++) {
				result[i-start] = target[i];
			}
			return result;
	}
	 
    private  ApiInfo getApiInfo(String title, String description, String version, String author, String email) {
        return new ApiInfoBuilder()
                .title(title)
                .description(description) 
                .version(version)
                .contact(new Contact(author,"", email))
                .build();
    }

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException { 
		beanFactory.registerSingleton(dt.getClass().getCanonicalName(), dt);
	}
	
	@Data
	public static class DocketInfo{
		private String groupName;
		private ApiInfo apiInfo;
		private Class<?> targetClass;
		private Tag [] tags;
	}

	public static class DocketWrapper extends Docket{ 
		
		public DocketWrapper(DocketInfo docketInfo) {
			super(dt);
			super.apiInfo(docketInfo.getApiInfo()) 
            .groupName(docketInfo.getGroupName())
            .forCodeGeneration(false)
            .select()
                .apis(ClassSelector.classCanAssignableTo(docketInfo.getTargetClass()))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))  
                .build();
			if(docketInfo.tags!=null && docketInfo.tags.length>0) {
				super.tags(docketInfo.tags[0], copyArray(docketInfo.tags, 1, docketInfo.tags.length));
			} 
		} 
	}
	
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		collect();
		for(DocketInfo docketInfo : docketInfos) {   
			BeanDefinitionBuilder bdb =  BeanDefinitionBuilder.genericBeanDefinition(DocketWrapper.class);
			bdb.addConstructorArgValue(docketInfo);
			registry.registerBeanDefinition(docketInfo.getGroupName()+"Docket", bdb.getBeanDefinition());
		} 
		registry.removeBeanDefinition("inMemorySwaggerResourcesProvider");
		registry.removeBeanDefinition("swaggerOperationTagsReader");
		registry.removeBeanDefinition("apiListingScanner");
	} 
	 
	public Object getDocketPropertyByName(DocketWrapper docket, String name) { 
		Field fields[]  = docket.getClass().getDeclaredFields();
		Field.setAccessible(fields, true);
		for(Field field : fields) {
			if(field.getName().equals(name)) {
				try {
					return field.get(docket);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null; 
	}
	
}
