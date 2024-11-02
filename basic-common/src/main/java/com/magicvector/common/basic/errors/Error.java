package com.magicvector.common.basic.errors;

import lombok.Data;

import java.io.Serializable;

@Data
public class Error implements Serializable {
    /**
     * The error code
     */
    private String code;
    /**
     * The error message shown to our users.
     */
    private String userReadInfo;
    /**
     * The error message shown to our inner developers.
     */
    private String developerReadInfo;


    public Error(String code, String developerReadInfo, String userReadInfo){
        this.code = code;
        this.userReadInfo = userReadInfo;
        this.developerReadInfo = developerReadInfo;
    }
}