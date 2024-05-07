package com.common.business.wrapper;

import java.util.List;

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
	
	public FeignInvoke(String className, String methodName, List<Object> param) {
		this.className = className;
		this.methodName = methodName;
		this.param = param;
	}

	public FeignInvoke() {
		
	}
}
