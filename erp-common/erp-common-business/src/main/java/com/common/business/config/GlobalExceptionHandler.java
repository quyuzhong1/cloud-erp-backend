package com.common.business.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.StringUtils;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.List;

/**
 * @Classname: GlobalExceptionHandler
 * @Description: TODO
 * @CreateTime: 2023-04-13  19:34
 * @Author: zhangchunlin
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ServiceException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult resolveException(ServiceException e) {
        log.error("系统异常：", e);
        return new ApiResult(e);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult resolveException(MethodArgumentNotValidException e) {
        log.error("系统异常：", e);
        ApiResult result = new ApiResult();
        result.setCode(ApiError.ERROR_1000.code);
        result.setMsg(e.getBindingResult().getFieldError().getDefaultMessage());
        return result;
    }


    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ApiResult resolveException(HttpRequestMethodNotSupportedException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_405);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ApiResult resolveException(MissingServletRequestParameterException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_600);
    }

    @ExceptionHandler(value = BindException.class)
    public ApiResult resolveException(BindException e) {
        log.error("系统异常：", e);
        List<ObjectError> fieldErrors = e.getBindingResult().getAllErrors();
        if (fieldErrors != null && fieldErrors.size() > 0) {
            ObjectError objectError = ValidatorUtil.getPermanentError(fieldErrors);
            return ApiResult.error(ApiError.ERROR_400.code, objectError.getDefaultMessage());
        }
        return ApiResult.error(ApiError.ERROR_400);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ApiResult resolveException(DataIntegrityViolationException e) {
        log.error("系统异常：", e);
        if(e.getMessage().contains("value too long")) {
            return ApiResult.error(ApiError.ERROR_1025);
        } else {
            return ApiResult.error(ApiError.ERROR_1002);
        }
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ApiResult resolveException(MaxUploadSizeExceededException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_1021);
    }


    @ExceptionHandler(value = IllegalStateException.class)
    public ApiResult resolveException(IllegalStateException e) {
        log.error("系统异常：", e);
        if (!StringUtils.isEmpty(e.getMessage()) && e.getMessage().contains("No instances available for")) {
            return ApiResult.error(ApiError.ERROR_1023);
        } else {
            return ApiResult.error(ApiError.Default);
        }
    }

    @ExceptionHandler(value = MissingRequestHeaderException.class)
    public ApiResult resolveException(MissingRequestHeaderException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_400);
    }

    @ExceptionHandler(value = DuplicateKeyException.class)
    public ApiResult resolveException(DuplicateKeyException e) {
        log.error("系统异常:", e);
        if (!StringUtils.isEmpty(e.getMessage()) && e.getMessage().contains("Duplicate entry")
                && e.getMessage().contains("for key")) {
            String duplicateKey = e.getMessage().substring(e.getMessage().indexOf("Duplicate entry") + 15,e.getMessage().indexOf("for key"));
            return ApiResult.error(ApiError.ERROR_1024.code, StrUtil.format("数据【{}】重复，请修改后再提交",duplicateKey));
        } else {
            return ApiResult.error(ApiError.ERROR_1024);
        }
    }

    @ExceptionHandler(value = IllegalMonitorStateException.class)
    public ApiResult resolveException(IllegalMonitorStateException ex) {
        log.error("系统异常:", ex);
        if (StrUtils.isNotEmpty(ex.getMessage()) && ex.getMessage().contains("attempt to unlock lock, not locked by current thread by node id")) {
            return ApiResult.error(ApiError.ERROR_1026);
        } else {
            return ApiResult.error(ApiError.Default);
        }
    }


    /**
     * 兜底的异常
     * @param e
     * @return
     */
    @Profile(value = {"uat", "prod"})
    @ExceptionHandler(Exception.class)
    public ApiResult resolveException(Exception e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.Default);
    }

}