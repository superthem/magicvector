package cn.magicvector.common.basic.util;

public class UrlUtil {

	public static String getRoute(String uri){
		int index = uri.indexOf('?');
		if( index >= 0 ){
			return uri.substring(0, index);
		}
		return uri;
	}
	 
	public static String getDomain(String uri) {
		uri = uri.replace("http://", "").replace("https://", "");
		int index = uri.indexOf('/');
		String domain = uri;
		if(index  >= 0) {
			domain = uri.substring(0, index);
		}
		if(domain.startsWith("127.0.0.1") || domain.startsWith("localhost"))
			domain = "localhost";
		if(domain.contains(":")) {
			index = domain.indexOf(":");
			domain = uri.substring(0, index);
		}
		return domain; 
	}
	
	public static String getRootDomain(String uri) {
		String domain =  getDomain(uri);
		if(isIp(domain))
			return domain;
		int index = domain.indexOf('.');
		if(index > 0) {
			return domain.substring(index+1, domain.length());
		} 
		return domain; 
	}
	
	
	private static boolean isIp(String domain) {
		boolean flag = false;
		for(char item : domain.toCharArray()) {
			if(item >= '0' && item <= '9' || item == '.') {
				continue;
			}
			return false;
		}
		return true;
	}
	public static String getRootCookieDomain(String uri) {
		String domain =  getRootDomain(uri);  
		return domain; 
	} 
}
