package cn.magicvector.common.basic.exceptions;


import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.errors.Error;
import cn.magicvector.common.basic.util.S;
import com.github.tbwork.anole.loader.util.JSON;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Use this Exception rather than the java's RuntimeException,
 * only MagicExceptions could be delivered among rpc calls.
 * @author tommy.tang
 */
@Data
public class MagicException extends RuntimeException{

    private MagicExceptionRaw magicExceptionRaw;

    public MagicException(){
        magicExceptionRaw = new MagicExceptionRaw();
        magicExceptionRaw.setError(Errors.UNKNOWN_ERROR);
    }

    public MagicException(Error error){
        super(String.format("[%s]-%s", error.getCode(), error.getDeveloperReadInfo()));
        magicExceptionRaw = new MagicExceptionRaw();
        magicExceptionRaw.setError(error);
    }

    public MagicException(Error error, String message){
        super(String.format("[%s]-%s. Details: %s", error.getCode(), error.getDeveloperReadInfo(), message));
        magicExceptionRaw = new MagicExceptionRaw();
        magicExceptionRaw.setError(error);
        magicExceptionRaw.setDetailMessage(message);
    }

    public MagicException(Error error, String messagePattern, String ... parameters){
        super(String.format("[%s]-%s. Details: %s", error.getCode(), error.getDeveloperReadInfo(),
                S.isEmpty(messagePattern) ? "" : String.format(messagePattern.replace("{}", "%s"), parameters)));
        magicExceptionRaw = new MagicExceptionRaw();
        magicExceptionRaw.setError(error);
        String detailMessage = String.format("[%s]-%s. Details: %s", error.getCode(), error.getDeveloperReadInfo(),
                S.isEmpty(messagePattern) ? "" : String.format(messagePattern.replace("{}", "%s"), parameters));
        magicExceptionRaw.setDetailMessage(detailMessage);
    }


    public String getErrorCode(){
        return magicExceptionRaw.getError().getCode();
    }

    public String json(){
         return JSON.toJSONString(magicExceptionRaw);
    }
}
