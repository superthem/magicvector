package com.magicvector.common.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The context parameters pass from the front-end to the last service
 * on the call chain.
 */
@Data
public class ContextParam implements Serializable {

    /**
     * 当前登录的用户
     */
    private CurrentUser user;

    /**
     * 租户ID，SaaS系统使用
     */
    private String tenantId;

    /**
     * RPC调用链跟踪ID，从应用层生成后，不再改变
     */
    private String traceId;

    /**
     * 请求ID，全局唯一
     */
    private String requestId;


    /**
     * 使用方自定义透传的参数，注意大小，太大了会导致性能降低
     */
    private Map<String, String> ext;

    public ContextParam(){
        ext = new HashMap<>();
    }

}
