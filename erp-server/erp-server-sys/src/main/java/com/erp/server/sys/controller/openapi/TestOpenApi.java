package com.erp.server.sys.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.OpenApiReqDTO;
import com.erp.server.sys.config.OpenApi;

@OpenApi
public class TestOpenApi {
	
	@OpenApi("testVoid")
	public void testVoid() {
		
	}
	
	@OpenApi("testString")
	public String testString() {
		return "测试返回String";
	}
	
	@OpenApi("testDto")
	public ApiResult<?> testDto() {
		ApiResult success = ApiResult.success();
		success.setData("测试返回testDto");
		return success;
	}
	
	@OpenApi("test")
	public OpenApiReqDTO test(OpenApiReqDTO req) {
		req.setTimestamp(System.currentTimeMillis());
		return req;
	}
}
