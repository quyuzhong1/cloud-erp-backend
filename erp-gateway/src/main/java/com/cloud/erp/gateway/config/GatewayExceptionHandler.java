package com.cloud.erp.gateway.config;

import java.util.*;

import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.utils.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * gateway api异常处理
 * @Classname: GatewayExceptionConfig
 * @CreateTime: 2023-06-16  14:46
 * @Author: zhangchunlin
 */
@Slf4j
public class GatewayExceptionHandler extends DefaultErrorWebExceptionHandler {

	public GatewayExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
								   ErrorProperties errorProperties, ApplicationContext applicationContext) {
		super(errorAttributes, resourceProperties, errorProperties, applicationContext);
	}

	/**
	 * 获取异常属性
	 */
	@Override
	protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		ApiError defaultError = ApiError.Default;
		String code = StrUtils.null2EmptyWithTrim(defaultError.code);
		String errorMessage = defaultError.msg;
		Map<String, Object> map = new HashMap<>(2);
		Throwable error = super.getError(request);
		log.error(StrUtil.format("网关异常，请求地址：{}",request.exchange().getRequest().getURI()),error);
		if (error instanceof org.springframework.cloud.gateway.support.NotFoundException) {
			ApiError apiError503 = ApiError.ERROR_1023;
			code = StrUtils.null2EmptyWithTrim(apiError503.code);
			errorMessage = apiError503.msg;
		}
		map.put("code", code);
		map.put("message", errorMessage);
		map.put("data", null);
		map.put("success", false);
		return map;
	}

	/**
	 * 指定响应处理方法为JSON处理的方法
	 * @param errorAttributes
	 */
	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}

	/**
	 * 根据code获取对应的HttpStatus
	 * @param errorAttributes
	 * @return
	 */
	@Override
	protected int getHttpStatus(Map<String, Object> errorAttributes) {
		return HttpStatus.OK.value();
	}

}

