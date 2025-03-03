package com.magicvector.common.service.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(
        description = "分页查询参数"
)
@Data
public class PageQuery {

    @ApiModelProperty("当前页")
    private Integer current = 1;

    @ApiModelProperty("每页的数量")
    private Integer size = 10;
}