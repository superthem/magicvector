package com.magicvector.common.basic.context;


import com.magicvector.common.basic.model.ContextParam;
import com.magicvector.common.basic.model.CurrentUser;
import org.slf4j.MDC;

public class GlobalContext {

	private static final String REQUEST_ID = "requestId";
	private static final String LOG_TRACE_ID = "traceId";
	private static final ThreadLocal<ContextParam> threadParam = new ThreadLocal<ContextParam>();

	public static void setCurrentUser(CurrentUser currentUser) {
		ContextParam temp = getThreadParamInstance();
		temp.setUser(currentUser);
	}

	public static CurrentUser getCurrentUser() {
		ContextParam contextParam = getThreadParamInstance();
		return contextParam.getUser();
	}

	public static void setTraceId(String traceId) {
		MDC.put(LOG_TRACE_ID, traceId);
		ContextParam contextParam = getThreadParamInstance();
		contextParam.setTraceId(traceId);
	}

	public static String getTraceId() {
		ContextParam contextParam = getThreadParamInstance();
		return contextParam.getTraceId();
	}

	public static void setRequestId(String requestId) {
		MDC.put(REQUEST_ID, requestId);
		ContextParam contextParam = getThreadParamInstance();
		contextParam.setRequestId(requestId);
	}

	public static String getRequestId() {
		ContextParam contextParam = getThreadParamInstance();
		return contextParam.getRequestId();
	}

	public static void setTenantId(String tenantId) {
		ContextParam contextParam = getThreadParamInstance();
		contextParam.setTenantId(tenantId);
	}

	public static String getTenantId() {
		ContextParam contextParam = getThreadParamInstance();
		return contextParam.getTenantId();
	}

	public static void setExtContextVariable(String variableName, String value){
		ContextParam contextParam = getThreadParamInstance();
		contextParam.getExt().put(variableName, value);
	}

	public static String getExtContextVariable(String variableName){
		ContextParam contextParam = getThreadParamInstance();
		return contextParam.getExt().get(variableName);
	}

	public static void clear() {
		threadParam.remove();
	}

	public static ContextParam getContext(){
		return getThreadParamInstance();
	}

	public static void setContext(ContextParam context){
		threadParam.set(context);
	}


	private static ContextParam getThreadParamInstance() {
		ContextParam temp =threadParam.get();
		if(temp == null) {
			temp = new ContextParam();
			threadParam.set(temp);
		}
		return temp;
	}
}
