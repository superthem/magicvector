package cn.magicvector.common.application.model;

import lombok.Data;

@Data
public class SsoCheckResult {

    /**
     * true-已登陆，false-未登录
     */
    private Boolean status;

    /**
     * 登录页地址
     */
    private String loginPageUrl;
}
