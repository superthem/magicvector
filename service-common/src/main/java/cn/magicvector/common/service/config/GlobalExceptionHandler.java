package cn.magicvector.common.service.config;

import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@Order(Integer.MIN_VALUE)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object processAllExceptions(Exception exception) {
        log.error("unknown exception ", exception);
        if (exception instanceof MagicException) {
            return exception;
        } else if(exception instanceof NullPointerException) {
            return new MagicException(Errors.LOGIC_ERROR, exception.getMessage());
        } else if(exception instanceof SQLException) {
            return new MagicException(Errors.SQL_ERROR, exception.getMessage());
        }
        return new MagicException(Errors.UNKNOWN_ERROR, exception.getMessage());
    }
}
