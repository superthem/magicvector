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

    /**
     * The system name;
     */
    private String system;
    /**
     * The business line name;
     */
    private String businessLine;

    public Error(String code, String developerReadInfo, String userReadInfo, String system, String businessLine){
        this.code = code;
        this.system = system;
        this.businessLine = businessLine;
        this.userReadInfo = userReadInfo;
        this.developerReadInfo = developerReadInfo;
    }
}