package com.magicvector.common.application.login.impl;

import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.application.config.StaticConfig;
import com.magicvector.common.application.ext.UserLoginService;
import com.magicvector.common.application.login.UserLoginController;
import com.magicvector.common.application.model.Request;
import com.magicvector.common.application.model.Response;
import com.magicvector.common.application.model.SsoCheckResult;
import com.magicvector.common.application.model.UserLoginDTO;
import com.magicvector.common.application.util.TokenGenerator;
import com.magicvector.common.basic.cache.Cache;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.model.CurrentUser;
import com.magicvector.common.basic.util.Asserts;
import com.magicvector.common.basic.util.S;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Map;

@RestController
@RequestMapping("/user")
@ConditionalOnProperty(name = "mv.embedded.login.enabled", havingValue = "true", matchIfMissing = true)
public class UserLoginControllerImpl implements UserLoginController {

    @Autowired
    @Qualifier("baseCache")
    private Cache cache;

    @Autowired(required = false)
    private UserLoginService userLoginService;

    @Override
    public Response<CurrentUser> login(Request<UserLoginDTO> request) {
        Asserts.assertTrue(request.getParameter()!=null, "登录参数不为空！");

        UserLoginDTO userLoginDTO = request.getParameter();
        String phone = userLoginDTO.getPhoneNo();
        String username = userLoginDTO.getUsername();
        String email = userLoginDTO.getEmail();
        String code = userLoginDTO.getCode();
        String password = userLoginDTO.getPasswordMd5();
        Map<String, Object> userProps = null;
        if(S.isNotEmpty(password)){
            if(S.isNotEmpty(username)){
                userProps = userLoginService.loginByUsernameAndPassword(username, password);
            }
            else if(S.isNotEmpty(phone)){
                userProps = userLoginService.loginByPhoneAndPassword(phone, password);
            }
            else if(S.isNotEmpty(email)){
                userProps = userLoginService.loginByEmailAndPassword(email, code);
            }
            else{
                throw new MagicException(Errors.ILLEGAL_PARAMETER, "无法检测到当前的登录验证方式！密码和验证码均为空！");
            }
        }
        else if(S.isNotEmpty(code)){
            userProps = userLoginService.loginByPhoneAndCode(phone, code);
        }
        else{
            throw new MagicException(Errors.ILLEGAL_PARAMETER, "无法检测到当前的登录验证方式！密码和验证码均为空！");
        }

        if(userProps != null){

            Asserts.assertTrue( userProps.get("id") !=null && S.isNotEmpty(userProps.get("id").toString()), "用户属性必须指定id字段，代表用户的唯一标识！");
            String token = TokenGenerator.generateToken();
            String cacheKey = getSessionKey(token);

            CurrentUser result = new CurrentUser();
            result.setToken(token);
            result.setUserProps(userProps);
            cache.set(cacheKey, result, Anole.getLongProperty("user.session.cache.time", 604800000L ));
            return Response.success(result);
        }

        return Response.success(null);
    }

    @Override
    public Response<Boolean> logout(Request<Void> request) {
        String token =  request.getToken();
        String cacheKey = getSessionKey(token);
        if(cache.get(cacheKey) == null){
            return Response.success(false);
        }
        else{
            cache.remove(cacheKey);
            return Response.success(true);
        }
    }

    private String getSessionKey(String token){
        return StaticConfig.SESSION_CACHE_GROUP_NAME+token;
    }

    @Override
    public Response<CurrentUser> getUserInfo(Request<Void> request) {

        String token = request.getToken();
        String cacheKey = getSessionKey(token);
        CurrentUser cachedUser = cache.get(cacheKey);

        if( cachedUser != null){
            userLoginService.processUserInfo(cachedUser);
            return  Response.success(cachedUser);
        }

        CurrentUser failEmptyUser = new CurrentUser();
        failEmptyUser.setLoginUrl(userLoginService.getSsoLoginUrl());
        return Response.success(failEmptyUser);
    }

}
