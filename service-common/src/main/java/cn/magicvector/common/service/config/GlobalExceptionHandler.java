package cn.magicvector.common.service.config;

import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@Order(Integer.MIN_VALUE)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    // 1. 返回类型改为 ResponseEntity，以便控制状态码
    public ResponseEntity<Object> processAllExceptions(Exception exception) {
        MagicException magicException;

        // 2. 统一封装异常逻辑
        if (exception instanceof MagicException) {
            magicException = (MagicException) exception;
        } else if (exception instanceof NullPointerException) {
            magicException = new MagicException(Errors.LOGIC_ERROR, exception.getMessage());
        } else if (exception instanceof SQLException) {
            magicException = new MagicException(Errors.SQL_ERROR, exception.getMessage());
        } else {
            magicException = new MagicException(Errors.UNKNOWN_ERROR, exception.getMessage());
        }

        return ResponseEntity
                .status(500)
                .body(magicException.json());
    }
}
