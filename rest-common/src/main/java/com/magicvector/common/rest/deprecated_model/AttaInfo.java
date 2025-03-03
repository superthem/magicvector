package com.magicvector.common.rest.deprecated_model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("阿里云Oss附件信息")
public class AttaInfo {
    @ApiModelProperty("文件地址")
    private String url;
    @ApiModelProperty("文件名")
    private String key;
    @ApiModelProperty("业务类型")
    private String biz;
    @ApiModelProperty("扩展信息")
    private String extension;
    @ApiModelProperty("文件名")
    private String fileName;
    @ApiModelProperty("文件目录")
    private String path;
}