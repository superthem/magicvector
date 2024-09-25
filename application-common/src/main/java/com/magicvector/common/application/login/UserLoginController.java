package com.magicvector.common.application.login;

import com.magicvector.common.application.annotation.user.Private;
import com.magicvector.common.application.annotation.user.Public;
import com.magicvector.common.application.model.Request;
import com.magicvector.common.application.model.Response;
import com.magicvector.common.application.model.SsoCheckResult;
import com.magicvector.common.application.model.UserLoginDTO;
import com.magicvector.common.basic.model.CurrentUser;
import org.checkerframework.checker.units.qual.Current;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserLoginController {


    @Public
    @PostMapping("/login")
    Response<CurrentUser> login(@RequestBody Request<UserLoginDTO> request);

    @Private
    @PostMapping("/logout")
    Response<Boolean> logout(@RequestBody Request<Void> request);


    @Public
    @PostMapping("/checkSsoLogin")
    Response<SsoCheckResult> checkSsoLogin(@RequestBody Request<Void> request);
}
