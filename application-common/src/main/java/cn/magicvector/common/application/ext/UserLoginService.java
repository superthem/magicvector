package cn.magicvector.common.application.ext;


import cn.magicvector.common.basic.model.CurrentUser;

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
     * 微信静默授权码登录。不会自动注册用户：若找不到对应用户，实现可返回 null 或抛出 MagicException；其它失败情况请抛出 MagicException。
     * @param silentAuthCode 用户的静默授权码
     * @param extraInfo 附加信息（可选扩展字段）
     * @return
     */
    public Map<String, Object> loginByWechatSilentAuthCode(String silentAuthCode, Map<String, String> extraInfo);

    /**
     * 微信静默授权码 + 获取绑定手机号的授权码登录。与 {@link #loginByWechatSilentAuthCode} 不同：应自动注册不存在用户。登录失败请抛出 MagicException。
     * @param silentAuthCode 用户的静默授权码
     * @param phoneCode 获取微信绑定手机号的授权码
     * @param extraInfo 附加信息（可选扩展字段）
     * @return
     */
    public Map<String, Object> loginByWechatAuthAndPhoneCode(String silentAuthCode, String phoneCode, Map<String, String> extraInfo);

    /**
     * 检查用户是否登录，如果用户没有登录，获取登录地址。
     * @return
     */
    public String getSsoLoginUrl();

    /**
     * 在会话已存在的前提下，从远端或持久层重新拉取用户属性并返回。
     * 调用方需已校验 token 对应缓存会话有效；失败请抛出 {@link cn.magicvector.common.basic.exceptions.MagicException}。
     *
     * @param currentUser 当前缓存中的登录用户（含 token、既有 userProps 等）
     * @return 最新的用户属性 Map，将用于覆盖会话中的 userProps
     */
    Map<String, Object> refreshAndGetUserProps(CurrentUser currentUser);
}
