package com.magicvector.common.rpc.config;

import com.github.tbwork.anole.loader.util.JSON;
import com.magicvector.common.basic.exceptions.MagicException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class ClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String method, Response response){
        String message= null;
        try {
            message = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            message = String.format("%s方法调用出错，具体原因：%s", method, message) ;
        } catch (IOException e) {
            log.warn("{}方法调用出错，具体原因：{}", method, e.getMessage(), e);
        }
        return JSON.parseObject(message, MagicException.class);
    }

}
