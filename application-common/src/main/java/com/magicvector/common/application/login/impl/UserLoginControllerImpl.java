package com.magicvector.common.application.login.impl;

import com.magicvector.common.application.ext.UserLoginService;
import com.magicvector.common.application.login.UserLoginController;
import com.magicvector.common.application.model.Request;
import com.magicvector.common.application.model.Response;
import com.magicvector.common.application.model.SsoCheckResult;
import com.magicvector.common.application.model.UserLoginDTO;
import com.magicvector.common.basic.cache.Cache;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.model.CurrentUser;
import com.magicvector.common.basic.util.Asserts;
import com.magicvector.common.basic.util.S;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openapi/user")
public class UserLoginControllerImpl implements UserLoginController {

    @Autowired
    @Qualifier("redisCache")
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
        CurrentUser user = null;
        if(S.isNotEmpty(password)){
            if(S.isNotEmpty(username)){
                user = userLoginService.loginByUsernameAndPassword(username, password);
            }
            else if(S.isNotEmpty(phone)){
                user = userLoginService.loginByPhoneAndPassword(phone, password);
            }
            else if(S.isNotEmpty(email)){
                user = userLoginService.loginByEmailAndPassword(email, code);
            }
            else{
                throw new MagicException(Errors.ILLEGAL_PARAMETER, "无法检测到当前的登录验证方式！密码和验证码均为空！");
            }
        }
        else if(S.isNotEmpty(code)){
            user = userLoginService.loginByPhoneAndCode(phone, code);
        }
        else{
            throw new MagicException(Errors.ILLEGAL_PARAMETER, "无法检测到当前的登录验证方式！密码和验证码均为空！");
        }

        return Response.success(user);
    }

    @Override
    public Response<Boolean> logout(Request<Void> request) {
        return null;
    }


    @Override
    public Response<SsoCheckResult> checkSsoLogin(Request<Void> request) {
        return null;
    }

}
