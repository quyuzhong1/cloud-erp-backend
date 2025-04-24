package com.erp.server.auth.controller.openapi;

import org.springframework.validation.annotation.Validated;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.OpenApiReqDTO;
import com.erp.server.auth.config.OpenApi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@OpenApi
public class TestOpenApi {
	
	/**
	 * 无参无返回值例子
	 */
	@OpenApi("testVoid")
	public void testVoid() {
		log.debug("测试testVoid");
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
	public ApiResult testDto() {
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
	public OpenApiReqDTO test(@Validated OpenApiReqDTO req) {
		req.setTimestamp(System.currentTimeMillis());
		return req;
	}
}
