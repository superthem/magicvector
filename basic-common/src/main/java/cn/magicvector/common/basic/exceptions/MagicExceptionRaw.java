package cn.magicvector.common.basic.exceptions;

import cn.magicvector.common.basic.errors.Error;
import lombok.Data;

@Data
public class MagicExceptionRaw {
    private Error error;
    private String detailMessage;

}
