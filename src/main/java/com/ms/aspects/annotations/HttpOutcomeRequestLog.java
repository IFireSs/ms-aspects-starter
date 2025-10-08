package com.ms.aspects.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpOutcomeRequestLog {
    String value() default "INFO";
}
