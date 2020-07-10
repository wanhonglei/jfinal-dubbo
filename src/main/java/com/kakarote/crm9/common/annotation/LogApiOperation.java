package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

/**
 * @author liming.guo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE,ElementType.PARAMETER })
@Inherited
@Documented
public @interface LogApiOperation {

     String methodName() default "";

}
