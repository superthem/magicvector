package cn.magicvector.common.rest.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomCounter {
    String resource();

    String name();
}
