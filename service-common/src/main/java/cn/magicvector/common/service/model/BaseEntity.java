package cn.magicvector.common.service.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class BaseEntity<T extends Model> extends Model {

    @JsonSerialize(
            using = ToStringSerializer.class
    )
    @ApiModelProperty("创建人")
    @TableField(
            value = "creator",
            fill = FieldFill.INSERT
    )
    private String creator;
    @DateTimeFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(
            timezone = "GMT+8",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @ApiModelProperty("创建时间")
    @TableField(
            value = "create_time",
            fill = FieldFill.INSERT
    )
    private Date createTime;
    @JsonSerialize(
            using = ToStringSerializer.class
    )
    @ApiModelProperty("更新人")
    @TableField(
            value = "last_operator",
            fill = FieldFill.INSERT_UPDATE
    )
    private String lastOperator;
    @DateTimeFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(
            timezone = "GMT+8",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @ApiModelProperty("更新时间")
    @TableField(
            value = "update_time",
            fill = FieldFill.INSERT_UPDATE
    )
    private Date updateTime;

}