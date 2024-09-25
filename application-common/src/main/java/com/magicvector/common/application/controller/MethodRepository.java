package com.magicvector.common.application.controller;

import java.lang.reflect.Method;

public interface MethodRepository {


    /**
     * Get the target method by url path.
     * @param path
     * @return
     */
    TargetMethod getMethodByPath(String path);


    /**
     * Register a method to the repository.
     *
     * @param path the path
     * @param method the method
     * @param owner the owner
     */
    void register(String path, Method method, Object owner);


}
