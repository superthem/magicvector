package com.magicvector.common.application.ext;


import com.magicvector.common.basic.model.CurrentUser;

import java.util.Map;

public interface UserLoginService {

    /**
     * 进一步处理用户信息（成功时回调，用于更新一些易变的用户信息）
     * @param currentUser
     */
    public void processUserInfo(CurrentUser currentUser);

    /**
     * 电话号码登录。登录失败请抛出MagicException异常
     * @param phone
     * @param code
     * @return
     */
    public Map<String, Object> loginByPhoneAndCode(String phone, String code, Map<String, String> extraInfo);

    /**
     * 用户名密码登录，建议对传入对密码MD5加盐做二次MD5.登录失败请抛出MagicException异常
     * @param username 用户名
     * @param passwordMd5 前端必须加密为MD5后传入
     * @return
     */
    public Map<String, Object> loginByUsernameAndPassword(String username, String passwordMd5, Map<String, String> extraInfo);

    /**
     * 邮箱+密码登录，建议对传入对密码MD5加盐做二次MD5.登录失败请抛出MagicException异常
     * @param email 邮箱地址
     * @param passwordMd5 前端必须加密为MD5后传入
     * @return
     */
    public Map<String, Object> loginByEmailAndPassword(String email, String passwordMd5, Map<String, String> extraInfo);

    /**
     * 电话号码+密码登录，建议对传入对密码MD5加盐做二次MD5.登录失败请抛出MagicException异常
     * @param phone 电话号码
     * @param passwordMd5 前端必须加密为MD5后传入
     * @return
     */
    public Map<String, Object> loginByPhoneAndPassword(String phone, String passwordMd5, Map<String, String> extraInfo);


    /**
     * 检查用户是否登录，如果用户没有登录，获取登录地址。
     * @return
     */
    public String getSsoLoginUrl();
}
