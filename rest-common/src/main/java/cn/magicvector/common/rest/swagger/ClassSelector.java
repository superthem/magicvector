package cn.magicvector.common.rest.swagger;

import com.google.common.base.Predicate;
import springfox.documentation.RequestHandler;


public class ClassSelector {

	/**
	* Predicate that matches RequestHandler with given class of the handler method.
	* This predicate includes all request handlers matching the specified Class
	*
	* @param  clazz - target class
	* @return this
	*/
	public static Predicate<RequestHandler> classCanAssignableTo(final Class<?> clazz) {
		return input ->  clazz.isAssignableFrom(input.declaringClass());
	}
	  
}
