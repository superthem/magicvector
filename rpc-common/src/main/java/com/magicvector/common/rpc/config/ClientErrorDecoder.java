package com.magicvector.common.rpc.config;

import com.github.tbwork.anole.loader.util.JSON;
import com.google.gson.JsonObject;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import com.magicvector.common.basic.errors.Error;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class ClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String method, Response response){
        MagicException result = new MagicException();
        String responseStreamString = null;
        try {
            responseStreamString = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            JsonObject responseObject = getMessageObject(responseStreamString);
            if(responseObject.has("error") && responseObject.get("error").getAsString().trim().startsWith("{")){
                return JSON.parseObject(responseStreamString, MagicException.class);
            }
            else if(responseObject.has("error")
                    && responseObject.has("status")){

                String errorMessage =  responseObject.get("error").getAsString();
                if(responseObject.has("path")){
                    errorMessage = errorMessage + "("+responseObject.get("path").getAsString()+")";
                }

                Error error = new Error(
                        responseObject.get("status").getAsString(),
                        errorMessage,
                        errorMessage
                        );

                result.setError(error);
                return result;
            }
            else{
                Error error = new Error(
                        Errors.UNKNOWN_ERROR.getCode(),
                        "错误明细："+responseStreamString,
                        "出了点奇怪的错误，联系工作人员解决。"
                );
                result.setError(error);
                return result;
            }


        } catch (IOException e) {
            log.warn("{}方法调用出错，具体原因：{}", method, e.getMessage(), e);
            String message = method+ "方法调用出错，具体原因："+e.getMessage();
            Error error = new Error(
                    Errors.UNKNOWN_ERROR.getCode(),
                    message,
                    "出了点奇怪的错误，联系工作人员解决。"
            );
            return result;
        }

    }


    private JsonObject getMessageObject(String responseStreamString){
        return (JsonObject) JSON.parseObject(responseStreamString);
    }

}
