package com.ms.aspects.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Metric {
    String value() default "";
    long thresholdMs() default -1;
    boolean includeParams() default true;
}
