package com.magicvector.common.application.controller.impl;

import com.github.tbwork.anole.loader.util.JSON;
import com.magicvector.common.application.controller.BatchRequestItem;
import com.magicvector.common.application.controller.MethodRepository;
import com.magicvector.common.application.controller.MultipleCallController;
import com.magicvector.common.application.controller.TargetMethod;
import com.magicvector.common.application.model.Request;
import com.magicvector.common.application.model.Response;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.rest.annotation.SwaggerModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SwaggerModule(name = "组合调用接口", description = "将多个调用组合成一个调用进行同步调用")
public class MultipleCallControllerImpl implements MultipleCallController {

    @Autowired
    private MethodRepository methodRepository;

    @Override
    public Response<List<Response>> batchCall(Request<List<BatchRequestItem>> callItems) {

        List<Response> responses = new ArrayList<>();
        callItems.getParameter().stream().forEach(item->{
            TargetMethod targetMethod =  methodRepository.getMethodByPath(item.getPath());
            Response response = null;
            try {
                response = (Response) targetMethod.getMethod().invoke(targetMethod.getOwner(), item);
            } catch (Exception e) {
                log.error("Error occurs when do batch call, input is : \n {}", JSON.toJSONString(callItems),e);
                throw new MagicException(Errors.LOGIC_ERROR, "批量调用失败！");
            }
            responses.add(response);
        });
        return Response.success(responses);

    }
}
