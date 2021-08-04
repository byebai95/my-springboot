package com.bai.app.annotation;

import java.lang.annotation.*;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 14:30
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperatorAction {

    String value() default "";
}
