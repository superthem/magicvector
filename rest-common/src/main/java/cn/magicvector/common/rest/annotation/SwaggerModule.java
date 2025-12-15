package cn.magicvector.common.rest.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * <b>Please note</b> that one module is corresponding to one controller.
 * This annotation is used to define a module for a certain group of APIs.
 * 
 * @author tbwork
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented   
@Inherited
@RestController
public @interface SwaggerModule {

	/**
	 * The name of module. Please notice that the module names of different
	 * controllers should also differ from each other. The name will be shown
	 * in the <b>"spec" drop-list options</b> on the swagger-ui.html page. 
	 */
	public String name() default "";
	
	/**
	 * The detailed description of this module.
	 */
	public String description() default "";
	
	/**
	 * The version of APIs.
	 */
	public String version() default "New" ;
	
	/**
	 * The author or person in charge of this module. Please provider the full
	 * Chinese name so that others can contact you easily.
	 */
	public String author() default "unknown";
	
	/**
	 * The email of the author. Other users can contact the author by this email.
	 */
	public String email() default "bobo@lanehub.cn";
	  
}
