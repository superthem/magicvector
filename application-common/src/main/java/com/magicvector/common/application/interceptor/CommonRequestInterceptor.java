package com.magicvector.common.application.interceptor;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


public class CommonRequestInterceptor extends HandlerInterceptorAdapter{
  
	public static interface Interceptor{
		public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
		public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
		public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
		
	}
	
	private static List<Interceptor> interceptors;
	
	public CommonRequestInterceptor(){
		interceptors = new ArrayList<Interceptor>();
		//ADD initial interceptors here 
	}
	
	public static void addFromHead(Interceptor interceptor){
		interceptors.add(0, interceptor);
	}
	
	public static void append(Interceptor interceptor){
		interceptors.add(interceptor);
	}
	
	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
		for(Interceptor interceptor: interceptors) {
			boolean stepResult = interceptor.preHandle(httpServletRequest, httpServletResponse);
			if(!stepResult){
				return false;
			}
		}
        return true;
    }
 
	
	/**
	 * This implementation is empty.
	 */
	@Override
	public void postHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {
		for(Interceptor interceptor: interceptors) {
		   interceptor.postHandle(request, response); 
		} 
	}
	
	
	/**
	 * This implementation is empty.
	 */
	@Override
	public void afterCompletion(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		for(Interceptor interceptor: interceptors) {
			 interceptor.afterCompletion(request, response); 
		} 
	}
}
