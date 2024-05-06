package com.common.business.wrapper;

import java.util.LinkedHashMap;

import lombok.Data;

/**
 * 远程调用参数
 * @author Administrator
 *
 */
@Data
public class FeignInvoke {
	private String className;
	private String methodName;
	private LinkedHashMap<Class<?>, Object> param;
	
	public FeignInvoke(String className, String methodName, LinkedHashMap<Class<?>, Object> param) {
		this.className = className;
		this.methodName = methodName;
		this.param = param;
	}

	public FeignInvoke(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}
	
	public FeignInvoke() {
		
	}
}
