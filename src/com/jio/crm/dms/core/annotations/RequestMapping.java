package com.jio.crm.dms.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jio.crm.dms.core.HttpRequestMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {
	
    public String name() default "/";
    public HttpRequestMethod type() default HttpRequestMethod.GET;

}
