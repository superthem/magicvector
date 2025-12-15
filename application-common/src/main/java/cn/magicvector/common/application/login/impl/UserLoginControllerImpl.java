package cn.magicvector.common.application.login.impl;

import com.github.tbwork.anole.loader.Anole;
import cn.magicvector.common.application.config.StaticConfig;
import cn.magicvector.common.application.ext.UserLoginService;
import cn.magicvector.common.application.login.UserLoginController;
import cn.magicvector.common.application.model.*;
import cn.magicvector.common.application.util.TokenGenerator;
import cn.magicvector.common.basic.cache.Cache;
import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;
import cn.magicvector.common.basic.model.CurrentUser;
import cn.magicvector.common.basic.util.Asserts;
import cn.magicvector.common.basic.util.S;
import cn.magicvector.common.rest.annotation.SwaggerModule;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Service
@SwaggerModule(
    name = "内置用户服务",
    description = "提供常见的登录/登出等功能",
    author = "tommy.tesla"
)
@ConditionalOnProperty(name = "mv.embedded.login.enabled", havingValue = "true", matchIfMissing = true)
public class UserLoginControllerImpl implements UserLoginController {


    @Autowired
    @Qualifier("baseCache")
    private Cache cache;

    @Autowired(required = false)
    private UserLoginService userLoginService;


    @PostConstruct
    private void init(){
        log.info("内置用户认证服务已经启动");
    }

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
                userProps = userLoginService.loginByUsernameAndPassword(username, password, userLoginDTO.getExtraInfo());
            }
            else if(S.isNotEmpty(phone)){
                userProps = userLoginService.loginByPhoneAndPassword(phone, password, userLoginDTO.getExtraInfo());
            }
            else if(S.isNotEmpty(email)){
                userProps = userLoginService.loginByEmailAndPassword(email, code, userLoginDTO.getExtraInfo());
            }
            else{
                throw new MagicException(Errors.ILLEGAL_PARAMETER, "无法检测到当前的登录验证方式！密码和验证码均为空！");
            }
        }
        else if(S.isNotEmpty(code)){
            userProps = userLoginService.loginByPhoneAndCode(phone, code, userLoginDTO.getExtraInfo());
        }
        else{
            throw new MagicException(Errors.ILLEGAL_PARAMETER, "无法检测到当前的登录验证方式！密码和验证码均为空！");
        }

        if(userProps != null){
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
    public Response<Boolean> logout(Request<Empty> request) {
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
    public Response<CurrentUser> getUserInfo(Request<Empty> request) {

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
