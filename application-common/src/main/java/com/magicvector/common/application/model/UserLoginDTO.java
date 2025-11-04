package com.magicvector.common.application.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.*;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginDTO {

    @ApiModelProperty(value = "手机号登录时使用")
    private String phoneNo;

    @ApiModelProperty(value = "邮箱登录时使用")
    private String email;

    @ApiModelProperty(value = "用户名登录时使用")
    private String username;

    @ApiModelProperty(value = "登录验证码（手机/邮箱）")
    private String code;

    @ApiModelProperty(value = "登录密码的MD5")
    private String passwordMd5;

    @ApiModelProperty(value = "额外信息")
    private Map<String, String> extraInfo;

}
