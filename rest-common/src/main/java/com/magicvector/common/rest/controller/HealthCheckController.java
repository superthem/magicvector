package com.magicvector.common.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author shawn feng
 * @description
 * @date 2021/12/24 10:33
 */
public interface HealthCheckController {

    @GetMapping("/healthCheck")
    String healthCheck();
}
