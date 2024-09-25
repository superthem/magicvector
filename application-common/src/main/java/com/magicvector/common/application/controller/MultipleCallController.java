package com.magicvector.common.application.controller;

import com.magicvector.common.application.model.Request;
import com.magicvector.common.application.model.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping
public interface MultipleCallController {


    @PostMapping(path = "/batchCall")
    Response<List<Response>> batchCall(Request<List<BatchRequestItem>> callItems);


}
