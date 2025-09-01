package com.magicvector.common.application.ext;

import com.magicvector.common.basic.context.GlobalContext;

public interface AuthService {

    /**
     * 认证服务，请调用GlobalContext获取用户信息。
     */
    void checkAuth();

}
