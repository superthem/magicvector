package com.magicvector.common.application.gateway;

import com.github.tbwork.anole.loader.util.JSON;
import com.google.gson.JsonObject;
import com.magicvector.common.application.annotation.monitor.LogFormat;
import com.magicvector.common.application.annotation.user.Private;
import com.magicvector.common.application.annotation.user.Public;
import com.magicvector.common.application.ext.ContextExtSeedService;
import com.magicvector.common.application.model.Request;
import com.magicvector.common.application.model.Response;
import com.magicvector.common.application.config.StaticConfig;
import com.magicvector.common.application.util.ObjectUtil;
import com.magicvector.common.basic.cache.Cache;
import com.magicvector.common.basic.context.GlobalContext;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.model.CurrentUser;
import com.magicvector.common.basic.util.Asserts;
import com.magicvector.common.basic.util.S;
import com.magicvector.common.basic.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

/**
 * Provide gateway services such as uniform log recording, retrieving user
 * informations, basic validation, etc..
 * @author tommy.tang
 */
@Slf4j
@Aspect
@Order(0) //the last one
@Component
public class GatewayAspect {

	@Pointcut("execution(com.magicvector.common.application.model.Response com.magicvector..*Controller.*(..)) &&"
	         +"args(com.magicvector.common.application.model.Request)")
	private void restApi() {};

	@Autowired
	@Qualifier("baseCache")
	private Cache cache;

	@Autowired(required = false)
	private ContextExtSeedService contextExtSeedService;
	
	@Value("${magic.vector.gateway.log.enabled:true}")
	private Boolean logDetailEnabled;

	@Around("restApi()")
	public Object logRequestAndResponse(ProceedingJoinPoint joinPoint){
		// get the target class
		Object target = joinPoint.getTarget();
		// get the target method's name
		String methodName = joinPoint.getSignature().getName();
		// get arguments of the target method
		Object[] args = joinPoint.getArgs();
		// get paramterTypes of the target method
		Class<?> [] parameterTypes = ((MethodSignature)joinPoint.getSignature()).getMethod().getParameterTypes();
		Class<?> targetClass = target.getClass();

		Asserts.assertTrue(target.getClass().getInterfaces().length == 1, "The controller implement should have only one parent interface.");
		Class<?> targetInterface = target.getClass().getInterfaces()[0];

		Method targetMethod = ObjectUtil.getTargetMethod(target, args, methodName, parameterTypes);
		Method interfaceMethod = ObjectUtil.getInterfaceMethod(target, args, methodName, parameterTypes);


		boolean formatedLog = false;
		boolean separated = false;

		GlobalContext.clear();//against reuse of thread-local variables.
		//check whether the annotation is present on the target class
		if(targetInterface.isAnnotationPresent(LogFormat.class)){
			LogFormat logFormat = targetInterface.getAnnotation(LogFormat.class);
			if(logFormat != null )
				formatedLog = logFormat.enable();
			if(logFormat != null && logFormat.seperate())
				separated = logFormat.seperate();
		}

		//check whether the annotation is present on the target method
		if(interfaceMethod.isAnnotationPresent(LogFormat.class)){
			LogFormat logFormat = interfaceMethod.getAnnotation(LogFormat.class);
			if(logFormat != null)
				formatedLog = logFormat.enable();
			if(logFormat != null)
				separated = logFormat.seperate();
		}


		Logger logger = LoggerFactory.getLogger(targetClass);
		Request request = (Request) args[0];// the first argument must be the request
		Asserts.assertTrue(request != null, "The first argument of api should must be Request or its subclass.");
		Asserts.assertTrue(S.isNotEmpty(request.getTraceId()), "The front-end client must specify a trace id while calling any application services!" );

		if(logDetailEnabled && separated){// separated output
			if(formatedLog){// formated output
				logger.info("MethodName:{}; RequestId:{}\nRequest:\n{}\n",methodName, request.getRequestId(), JSON.toJSONString(request, true));
			}
			else{
				logger.info("MethodName:{}; RequestId:{}; Request:{}",methodName, request.getRequestId(), JSON.toJSONString(request, false));
			}
		}

		Response response = null;
		long currentTime = System.currentTimeMillis();
		//javax validation
		Map<String, String> violations = ValidateUtil.validate(request);
		if(violations.isEmpty()) {
			//execute method
			try {
				//validate - user login check
				boolean isClassPublic = targetInterface.isAnnotationPresent(Public.class);
				boolean isMethodPublic = interfaceMethod.isAnnotationPresent(Public.class);
				boolean isMethodPrivate = interfaceMethod.isAnnotationPresent(Private.class);
				boolean needLogin = (isClassPublic && isMethodPrivate) || (!isClassPublic && !isMethodPublic);
				// Seed key information to the global context.
				seedToContext(request);
				if(needLogin){//need login
				 	if( GlobalContext.getCurrentUser()== null ){
						throw new MagicException(Errors.USER_NOT_LOGIN);
					}
				 	// TODO: auth service check goes here
				}
				response = (Response) joinPoint.proceed();
			}
			catch (MagicException e){
				logger.error("Call {} failed. Message:{}", methodName, e.getMessage(), e);
				response = Response.fail(e);
			}
			catch (Exception e){
				logger.error("Call {} failed. Message:{}", methodName, e.getMessage(), e);
				response = Response.fail(Errors.LOGIC_ERROR, e.getMessage());
			}
			catch (Throwable e) {
				logger.error("Call {} failed. Message:{}", methodName, e.getMessage(), e);
				response = Response.fail(Errors.UNKNOWN_ERROR, e.getMessage());
			}
		}
		else {
			//fail to validate the request
			response = Response.fail(Errors.ILLEGAL_PARAMETER, getIllegalParameterDetails(violations));
		}

		response.setCostTime(System.currentTimeMillis() - currentTime);
		JsonObject jsonResponse = JSON.toJSON(response);

		if(logDetailEnabled){
			if(separated){
				jsonResponse.addProperty("requestId", request.getRequestId());
				if(formatedLog){// formated output
					logger.info("MethodName:{}; RequestId:{}\nResponse:\n{}\n", methodName,request.getRequestId(),  JSON.toJSONString(jsonResponse, true) );
				}
				else{
					logger.info("MethodName:{}; RequestId:{}; Response:{}", methodName,request.getRequestId(), JSON.toJSONString(jsonResponse, false));
				}
			}
			else{// output together
				if(formatedLog){// formated output
					logger.info("MethodName:{}; RequestId:{}\nRequest:{}\nGlobalContext:{}\nResponse:\n{}\n",methodName, request.getRequestId(), JSON.toJSONString(request, true), JSON.toJSONString(GlobalContext.getContext(), true), JSON.toJSONString(jsonResponse, true) );
				}
				else{
					logger.info("MethodName:{};RequestId:{}; Request:{}; GlobalContext:{}; Response:{}",methodName, request.getRequestId(), JSON.toJSONString(request, true), JSON.toJSONString(GlobalContext.getContext(),true), JSON.toJSONString(jsonResponse, true));
				}
			}
		}

		return response;
	}

	private String [] getIllegalParameterDetails(Map<String, String> validationResult){
		String [] result = new String[validationResult.entrySet().size()];
		int p = 0;
		for(Map.Entry<String,String> entry : validationResult.entrySet()){
			result[p++] = entry.getValue();
		}
		return result;
	}


	private void seedToContext(Request request) {
		CurrentUser currentUser = null;
		try{
			currentUser = cache.get(StaticConfig.SESSION_CACHE_GROUP_NAME + request.getToken());
			if(currentUser == null){
				//尚未登录
				return;
			}
		}catch (Exception ex){
			log.warn("Fail to get user info from cache. Details:{}", ex.getMessage());
		}

		GlobalContext.setCurrentUser(currentUser);

		GlobalContext.setRequestId(request.getRequestId());

		GlobalContext.setTraceId(request.getTraceId());

		// Ext variables
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (contextExtSeedService != null && attributes != null) {
			HttpServletRequest httpRequest = attributes.getRequest();
			Map<String, String> customVariables = contextExtSeedService.retrieve(request, httpRequest);
			for (String key : customVariables.keySet()) {
				GlobalContext.setExtContextVariable(key, customVariables.get(key));
			}
		}

		if(request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			Enumeration<String> getParams = httpServletRequest.getParameterNames();
			while (getParams.hasMoreElements()) {
				String paramName = getParams.nextElement();
				String[] paramValues = httpServletRequest.getParameterValues(paramName);
				if (paramValues != null && paramValues.length > 0) {
					if(paramName.startsWith("_")){
						paramName = paramName.substring(1);
					}
					GlobalContext.setExtContextVariable(paramName, paramValues[0]);
				}
			}
		}

	}

}
