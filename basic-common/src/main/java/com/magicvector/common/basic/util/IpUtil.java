package com.magicvector.common.basic.util;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

public class IpUtil {

    public static String getIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }

    /**
     * 把字符串IP转换成long
     *
     */
    public static long ip2long(@NotNull String ipStr){
        String[] ip = ipStr.split("\\.");
        return (Long.valueOf(ip[0])<<24) + (Long.valueOf(ip[1]) << 16)
                + (Long.valueOf(ip[2]) << 8 ) + Long.valueOf(ip[3]);
    }

    /**
     * 把IP的long值转变成字符串
     */
    public static String long2Ip(long ipLong){
        StringBuilder ip = new StringBuilder();
        ip.append(ipLong>>>24).append(".");
        ip.append((ipLong>>>16) & 0xFF).append(".");
        ip.append((ipLong>>>8) & 0xFF).append(".");
        ip.append(ipLong & 0xFF);
        return ip.toString();
    }
}
