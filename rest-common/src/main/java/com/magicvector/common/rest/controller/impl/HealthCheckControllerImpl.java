package com.magicvector.common.rest.controller.impl;

import com.magicvector.common.rest.controller.HealthCheckController;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shawn feng
 * @description
 * @date 2021/12/24 10:34
 */
@RestController
public class HealthCheckControllerImpl implements HealthCheckController {
    @Override
    public String healthCheck() {
        return "ok";
    }
}
