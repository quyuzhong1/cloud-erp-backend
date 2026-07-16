package com.common.business.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
	private List<Object> param;
	private List<String> parameterTypeNames;
	
	public FeignInvoke(String className, String methodName, List<Object> param) {
		this.className = className;
		this.methodName = methodName;
		this.param = param;
	}

	public FeignInvoke(String className, String methodName, List<Object> param, Class<?>... parameterTypes) {
		this(className, methodName, param);
		if(parameterTypes != null && parameterTypes.length > 0) {
			this.parameterTypeNames = Arrays.stream(parameterTypes)
					.map(Class::getName)
					.collect(Collectors.toCollection(ArrayList::new));
		}
	}

	public FeignInvoke() {
		
	}
}
