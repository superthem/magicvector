package cn.magicvector.common.basic.exceptions;


import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.errors.Error;
import cn.magicvector.common.basic.util.S;
import lombok.Data;

/**
 * Use this Exception rather than the java's RuntimeException,
 * only MagicExceptions could be delivered among rpc calls.
 * @author tommy.tang
 */
@Data
public class MagicException extends RuntimeException{

    private Error error;

    public MagicException(){
        error = Errors.UNKNOWN_ERROR;
    }

    public MagicException(Error error){
        super(String.format("[%s]-%s", error.getCode(), error.getDeveloperReadInfo()));
        this.error = error;
    }

    public MagicException(Error error, String message){
        super(String.format("[%s]-%s. Details: %s", error.getCode(), error.getDeveloperReadInfo(), message));
        this.error = error;
    }

    public MagicException(Error error, String messagePattern, String ... parameters){
        super(String.format("[%s]-%s. Details: %s", error.getCode(), error.getDeveloperReadInfo(),
                S.isEmpty(messagePattern) ? "" : String.format(messagePattern.replace("{}", "%s"), parameters)));
        this.error = error;
    }


    public String getErrorCode(){
        return error.getCode();
    }

}
