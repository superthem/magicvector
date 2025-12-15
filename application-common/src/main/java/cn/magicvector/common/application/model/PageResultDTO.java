package cn.magicvector.common.application.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "分页查询结果")
public class PageResultDTO<T> {

    private T data;

    private Long size;

    private Long current;

    private Long total;
}
