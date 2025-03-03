package com.magicvector.common.service.api;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel(
        description = "分页查询结果"
)
@Data
public class PageResult<T> {
    private T data;
    private Long size;
    private Long current;
    private Long total;
    private Long pages;
}
