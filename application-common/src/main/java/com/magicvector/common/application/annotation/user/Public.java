package com.magicvector.common.application.annotation.user;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented   
@Inherited
public @interface Public{

}
