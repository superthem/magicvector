package cn.magicvector.common.application.login;

import cn.magicvector.common.application.annotation.user.Private;
import cn.magicvector.common.application.annotation.user.Public;
import cn.magicvector.common.application.model.*;
import cn.magicvector.common.basic.model.CurrentUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserLoginController {


    @Public
    @PostMapping("/user/login")
    @ApiOperation(value = "登录", notes = "登录")
    Response<CurrentUser> login(@RequestBody Request<UserLoginDTO> request);

    @Private
    @PostMapping("/user/logout")
    @ApiOperation(value = "推出登录", notes = "推出登录")
    Response<Boolean> logout(@RequestBody Request<Empty> request);


    @Private
    @PostMapping("/user/getUserInfo")
    @ApiOperation(value = "根据token获取用户信息", notes = "根据token获取用户信息")
    Response<CurrentUser> getUserInfo(@RequestBody Request<Empty> request);

    @Private
    @PostMapping("/user/refreshAndGetUserInfo")
    @ApiOperation(value = "刷新并获取用户信息", notes = "校验 token 有效后拉取最新用户属性并写回会话缓存")
    Response<CurrentUser> refreshAndGetUserInfo(@RequestBody Request<Empty> request);

}
