package com.erp.server.sys.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.OpenApiReqDTO;
import com.erp.server.sys.config.OpenApi;

@OpenApi
public class TestOpenApi {
	
	/**
	 * 无参无返回值例子
	 */
	@OpenApi("testVoid")
	public void testVoid() {
		
	}
	
	/**
	 * 无参有返回值
	 * @return
	 */
	@OpenApi("testString")
	public String testString() {
		return "测试返回String";
	}
	
	/**
	 * 无参有ApiResult返回值
	 * @return
	 */
	@OpenApi("testDto")
	public ApiResult<?> testDto() {
		ApiResult success = ApiResult.success();
		success.setData("测试返回testDto");
		return success;
	}
	
	/**
	 * 有参有返回值
	 * @param req
	 * @return
	 */
	@OpenApi("test")
	public OpenApiReqDTO test(OpenApiReqDTO req) {
		req.setTimestamp(System.currentTimeMillis());
		return req;
	}
}
