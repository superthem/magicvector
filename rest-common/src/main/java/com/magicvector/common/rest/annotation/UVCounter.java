package com.magicvector.common.rest.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UVCounter {

    String page() default "all";

}
