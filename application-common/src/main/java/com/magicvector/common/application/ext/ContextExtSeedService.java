package com.magicvector.common.application.ext;

import com.magicvector.common.application.model.Request;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 额外的使用方自定义的上下文数据种植服务。
 */
public interface ContextExtSeedService {

    /**
     * @param request POST提交来的业务请求
     * @param httpRequest Http 请求体
     * @return
     */
    Map<String, String> retrieve(Request request, HttpServletRequest httpRequest);

}
