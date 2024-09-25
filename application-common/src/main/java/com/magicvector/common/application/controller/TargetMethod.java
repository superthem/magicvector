package com.magicvector.common.application.controller;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class TargetMethod {


    /**
     * Method.
     */
    private Method method;


    /**
     * Method's object owner
     */
    private Object owner;

}
