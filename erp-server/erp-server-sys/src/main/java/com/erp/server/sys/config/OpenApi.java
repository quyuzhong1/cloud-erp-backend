package com.erp.server.sys.config;

import java.lang.annotation.*;

import org.springframework.stereotype.Component;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface OpenApi {
	
	/**
	 * 服务名称
	 */
	String value() default "";

}
