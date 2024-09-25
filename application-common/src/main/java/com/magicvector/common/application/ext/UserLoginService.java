package com.magicvector.common.application.ext;


import com.magicvector.common.basic.model.CurrentUser;

public interface UserLoginService {

    /**
     * 电话号码登录。登录失败请抛出MagicException异常
     * @param phone
     * @param code
     * @return
     */
    public CurrentUser loginByPhoneAndCode(String phone, String code);

    /**
     * 用户名密码登录，建议对传入对密码MD5加盐做二次MD5.登录失败请抛出MagicException异常
     * @param username 用户名
     * @param passwordMd5 前端必须加密为MD5后传入
     * @return
     */
    public CurrentUser loginByUsernameAndPassword(String username, String passwordMd5);

    /**
     * 邮箱+密码登录，建议对传入对密码MD5加盐做二次MD5.登录失败请抛出MagicException异常
     * @param email 邮箱地址
     * @param passwordMd5 前端必须加密为MD5后传入
     * @return
     */
    public CurrentUser loginByEmailAndPassword(String email, String passwordMd5);

    /**
     * 电话号码+密码登录，建议对传入对密码MD5加盐做二次MD5.登录失败请抛出MagicException异常
     * @param phone 电话号码
     * @param passwordMd5 前端必须加密为MD5后传入
     * @return
     */
    public CurrentUser loginByPhoneAndPassword(String phone, String passwordMd5);
}
