package com.erp.server.sys.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OpenApi {
	
	/**
	 * 服务名称
	 */
	String value() default "";

}
