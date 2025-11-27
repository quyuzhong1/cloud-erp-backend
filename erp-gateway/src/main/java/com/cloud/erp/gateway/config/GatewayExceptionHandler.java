package com.cloud.erp.gateway.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.cloud.erp.gateway.utils.GatewayLocaleUtils;
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
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * gateway api异常处理
 * @Classname: GatewayExceptionConfig
 * @CreateTime: 2023-06-16  14:46
 * @Author: zhangchunlin
 */
@Slf4j
public class GatewayExceptionHandler extends DefaultErrorWebExceptionHandler {
	@Resource
	private GatewayLocaleUtils localeUtils;

	public GatewayExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
								   ErrorProperties errorProperties, ApplicationContext applicationContext) {
		super(errorAttributes, resourceProperties, errorProperties, applicationContext);
	}

	/**
	 * 获取异常属性
	 */
	@Override
	protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		String code = StrUtils.null2EmptyWithTrim(ApiError.DEFAULT.getCode());
		String errorMessage =localeUtils.getMessage(ApiError.DEFAULT, request.exchange().getRequest());
		Map<String, Object> map = new HashMap<>(4);
		Throwable error = super.getError(request);
		log.error(CharSequenceUtil.format("网关异常，请求地址：{}",request.exchange().getRequest().getURI()),error);
		// 1023服务暂时不可用
		if (error instanceof org.springframework.cloud.gateway.support.NotFoundException) {
			code = StrUtils.null2EmptyWithTrim(ApiError.ERROR_SERVICE_UNAVAILABLE.getCode());
			errorMessage = localeUtils.getMessage(ApiError.ERROR_SERVICE_UNAVAILABLE, request.exchange().getRequest());
		}
		// 404接口路径不存在
		if (error instanceof org.springframework.web.server.ResponseStatusException
				&& HttpStatus.NOT_FOUND.equals(((ResponseStatusException) error).getStatus())){
			code = String.valueOf(ApiError.ERROR_HTTP_NOT_FOUND.getCode());
			errorMessage = localeUtils.getMessage(ApiError.ERROR_HTTP_NOT_FOUND, request.exchange().getRequest());
		}
		map.put("code", code);
		map.put("msg", errorMessage);
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

