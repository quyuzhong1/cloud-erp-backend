package com.common.business.config;

import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.FeignServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Classname: GlobalExceptionHandler
 * @Description: TODO
 * @CreateTime: 2023-04-13  19:34
 * @Author: zhangchunlin
 */
@Slf4j
@RestControllerAdvice
public class FeignExceptionHandler {

    @ExceptionHandler({FeignServiceException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult resolveException(FeignServiceException  e) {
        log.error("系统异常：{}", e.getMsg(), e);
        ApiResult result = new ApiResult();
        result.setCode(e.getCode());
        result.setMsg(e.getMsg());
        return result;
    }
}