package com.magicvector.common.application.model;

import com.magicvector.common.basic.errors.Error;
import com.magicvector.common.basic.exceptions.MagicException;
import com.magicvector.common.basic.util.S;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "The basic contract content of server's responses.")
@Data
public class Response<T> implements Serializable {

	@ApiModelProperty(notes = "The result status of the current call. True means there is no runtime error, otherwise false.")
	private boolean success;

	@ApiModelProperty(notes = "The result data.")
	private T result;

	@ApiModelProperty(notes = "The time cost of the current call.")
	private Long costTime;

	@ApiModelProperty(notes = "The error code.")
	private String errorCode;

	@ApiModelProperty(notes = "Error messages.")
	private List<String> errorMessages;
	
	public Response(){
	}

	private Response(Error error){
		this(error, null);
	}

	private Response(Error error, String ... notes){
		this.errorCode = error.getCode();
		this.errorMessages = new ArrayList<>();
		if(notes == null || notes.length == 0){
			this.errorMessages.add(error.getUserReadInfo());
		}
		else{
			for(String note : notes){
				this.errorMessages.add(String.format("%s. %s", error.getUserReadInfo(), note));
			}
		}
		this.success = false;
	}

	private Response(MagicException e){
		this(e, null);
	}

	private Response(MagicException e, String userReadMessage){
		this.errorCode = e.getErrorCode();
		this.errorMessages = new ArrayList<>();
		if(S.isNotEmpty(userReadMessage)){
			this.errorMessages.add(e.getError().getUserReadInfo()+" 详情:"+userReadMessage);
		}
		else{
			this.errorMessages.add(e.getError().getUserReadInfo());
		}
		this.success = false;
	}


	public static <T> Response<T> success(T result){
		Response<T>  response = new Response<T>();
		response.setSuccess(true);
		response.setResult(result);
		return response;
	}

	public static <T> Response<T>  fail(MagicException e){
		return new Response<T>(e);

	}

	public static <T> Response<T>  fail(Error error){
		return  new Response<T> (error);
	}

	public static <T> Response<T>  fail(Error error, String ... message){
		return  new Response<T>(error, message);
	}

}
