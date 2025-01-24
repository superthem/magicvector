package com.magicvector.common.basic.errors;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Errors {
 
	public static Error SERVER_BUSY                 = new Error("0000001", "服务器繁忙", "服务器目前比较拥挤，请稍后再试！");

	public static Error SERVER_TIMEOUT              = new Error("0000002", "服务处理超时","服务器目前比较拥挤，请稍后再试！");

	public static Error AUTHORITY_FAILED            = new Error("0000003", "权限验证失败","您当前没有权限进行此操作哦！");

	public static Error BOOT_ABORTED                = new Error("0000004", "启动被终止", "");

	public static Error RESOURCE_FORBIDDEN          = new Error("0000005", "目标资源禁止访问 ","您当前没有权限访问此资源哦！");

	public static Error HTTP_METHOD_NOT_SUPPORTED   = new Error("0000006", "HTTP方法暂不支持", "");

	public static Error LOGIC_ERROR   			   = new Error("0000007", "业务逻辑处理失败", "系统发生异常");

	public static Error ILLEGAL_PARAMETER   	       = new Error("0000008", "参数校验失败", "亲，请填写正确格式的信息哦！");

	public static Error NOT_SUPPORTED   	      	   = new Error("0000009", "方法尚未支持", "这个方法暂时不支持调用呢！");

	public static Error BAD_DATA_FORMAT = new Error("0000010", "错误的数据格式", "错误的数据格式！");

	public static Error SQL_ERROR   	               = new Error("0000011", "持久化失败", "持久化失败！");

	public static Error CONTEXT_NOT_READY           = new Error("0000101", "未找到Spring上下文", "");

	public static Error USER_NOT_LOGIN              = new Error("0000111", "用户未登录", "您没有登录，请先登录！");

	public static Error BAD_JSON_REQUEST            = new Error("0000222", "请求体JSON格式错误", "");

	public static Error LOCK_RESOURCE_PARSE_ERROR  = new Error("0000301", "锁对象资源无法从参数中正确解析", "锁对象资源无法从参数中正确解析");

	public static Error DISTRIBUTED_LOCK_TIMEOUT_ERROR  = new Error("0000302", "分布式锁获取超时", "目前流量过高，服务器负载过大，请稍后再试！");

	public static Error RESOURCE_LIMITED_ERROR  = new Error("0000304", "限制性资源正在被访问中", "请勿多次提交！");


	public static Error NOT_EXIST                   = new Error("0000404", "目标不存在", "您访问的资源不存在哦！");

	public static Error CONFIG_NOT_COMPLETE        = new Error("0000993", "配置不完整", "存在部分启动所需的配置没有设置！");
	public static Error MAIN_CLASS_NOT_FOUND        = new Error("0000994", "找不到启动主类", "");

	public static Error MANIFEST_NOT_FOUND          = new Error("0000996", "找不到Manifest文件", "");

	public static Error CLASS_NOT_FOUND             = new Error("0000997", "找不到此JAVA类", "");

	public static Error UNKNOWN_BOOT_LAUNCHER       = new Error("0000998", "无法识别程序的加载器", "");

	public static Error UNKNOWN_ERROR = new Error("0000999", "未知错误", "发生了一点奇怪的错误，我们正在解决，请稍后。");

	private static final Map<String, Error> cachedCodeErrorMap = new ConcurrentHashMap<String, Error>();

	public static Error getErrorByCode(String code) {
		try{
			if( cachedCodeErrorMap == null ){
				synchronized (Errors.class){
					if(  cachedCodeErrorMap == null ){
						Class callerClass = new Throwable().getStackTrace()[1].getClass();
						Field [] fields = callerClass.getDeclaredFields();
						for(Field field : fields){
							if( field.getType().isAssignableFrom(Error.class) ){
								Error lanehubError = (Error) field.get(callerClass) ;
								cachedCodeErrorMap.put(lanehubError.getCode(), lanehubError);
							}
						}
					}
				}
			}
			if(cachedCodeErrorMap.containsKey(code)){
				return cachedCodeErrorMap.get(code);
			}
		}
		catch (Exception e){
			return UNKNOWN_ERROR;
		}
		return UNKNOWN_ERROR;
	}
}
