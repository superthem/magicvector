package cn.magicvector.common.application.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;


public class IpUtil {

    private  Boolean isLocalIp(String ip){
        if(ip == null){
            return true;
        }
        String[] first2Part = ip.split(".",2);
        if(first2Part[0].equals("10")|| first2Part[0].equals("127")  || (first2Part[0].equals("192")  && first2Part[1].equals("168")) || (first2Part[0].equals("172")  && Integer.parseInt(first2Part[1]) >= 16 && Integer.parseInt(first2Part[1]) < 32)){
            return true;
        }
        return false;
    }
    /**
     * 获取请求的ip
     */
    public String getRequestIp() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        String ip;
        String remoteIp = request.getRemoteAddr();
        String realIp = request.getHeader("x-real-ip");
        // 有的user可能使用代理，为处理用户使用代理的情况，使用x-forwarded-for
        if(isLocalIp(remoteIp) && isLocalIp(realIp)){
            if (request.getHeader("x-forwarded-for") == null) {
                ip = remoteIp;
            }else{
                ip = request.getHeader("x-forwarded-for").split(" ")[0];
            }
        }else{
            if(isLocalIp(realIp)){
                ip = remoteIp;
            }else{
                ip = realIp;
            }
        }
        return ip;


    }
}