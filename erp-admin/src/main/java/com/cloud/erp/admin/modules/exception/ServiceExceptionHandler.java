package com.cloud.erp.admin.modules.exception;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理类
 *
 * @Classname ServiceExceptionHandler
 * @Description TODO
 * @Date 2022-07-06 14:17
 * @Created by yl
 */

@RestControllerAdvice
public class ServiceExceptionHandler {


    /**
     * 捕获ServiceException
     *
     * @param e
     * @return
     */

    @ExceptionHandler({ServiceException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult serviceExceptionHandler(ServiceException e) {
        return new ApiResult(e);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult defaultErrorHandler(MethodArgumentNotValidException e) {
        ApiResult result = new ApiResult();
        result.setCode(ApiError.ERROR_1000.code);
        result.setMsg(e.getBindingResult().getFieldError().getDefaultMessage());
        return result;
    }
}
