package cn.magicvector.common.application.annotation.user;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented   
@Inherited
public @interface EnableUserInfo {

	
}
