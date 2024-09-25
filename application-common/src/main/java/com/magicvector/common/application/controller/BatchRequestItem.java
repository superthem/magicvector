package com.magicvector.common.application.controller;

import com.magicvector.common.application.model.Request;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "BatchRequestItem", description = "批量调用请求项")
public class BatchRequestItem implements Serializable {

    @ApiModelProperty(value = "path", name="path", notes = "接口地址", required = true)
    private String path;

    @ApiModelProperty(value = "request", name="request", notes = "请求", required = true)
    private Request request;

}
