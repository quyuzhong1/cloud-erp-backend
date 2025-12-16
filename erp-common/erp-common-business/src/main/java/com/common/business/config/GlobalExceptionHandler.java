package com.common.business.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.FeignServiceException;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MessageUtils;
import com.common.core.utils.ValidatorUtil;
import com.netflix.client.ClientException;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MappingException;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * @Classname: GlobalExceptionHandler
 * @CreateTime: 2023-04-13  19:34
 * @Author: zhangchunlin
 */
@Slf4j
@RestControllerAdvice(basePackages = {
        "com.erp.server.scm.controller.api",
        "com.erp.server.wms.controller.api",
        "com.erp.server.workflow.controller.api",
        "com.erp.server.auth.controller.api",
        "com.erp.server.bi.controller.api",
        "com.erp.server.plm.controller.api",
        "com.erp.server.sys.controller.api",
        "com.erp.server.oms.controller.api",
        "com.erp.server.tms.controller.api",
        "com.erp.server.srm.controller.api",
        "com.erp.server.dmp.controller.api",
        "com.erp.server.mrp.controller.api",

        "com.erp.server.scm.controller.pda",
        "com.erp.server.wms.controller.pda",
        "com.erp.server.workflow.controller.pda",
        "com.erp.server.auth.controller.pda",
        "com.erp.server.bi.controller.pda",
        "com.erp.server.plm.controller.pda",
        "com.erp.server.sys.controller.pda",
        "com.erp.server.oms.controller.pda"

})
public class GlobalExceptionHandler {
    // ===================== 业务与远程异常 ===================== //

    /** 本地业务异常（ServiceException） */
    @ExceptionHandler(ServiceException.class)
    public ApiResult<?> handleServiceException(ServiceException e, HttpServletResponse response) {
        log.error("[ServiceException] code={}, msg={}", e.getCode(), e.getMsg(), e);
        setHttpStatus(response, e.getCode());
        return buildResult(e.getCode(), e.getMsg(), e.getData());
    }

    /** Feign远程调用异常（FeignServiceException） */
    @ExceptionHandler(FeignServiceException.class)
    public ApiResult<?> handleFeignServiceException(FeignServiceException e, HttpServletResponse response) {
        log.warn("[FeignServiceException] code={}, msg={}", e.getCode(), e.getMsg(), e);
        setHttpStatus(response, e.getCode());
        return buildResult(e.getCode(), e.getMsg(), null);
    }

    // ===================== 参数与校验异常 ===================== //

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : MessageUtils.getMessage(ApiError.HTTP_BAD_REQUEST);
        log.warn("[MethodArgumentNotValidException] {}", msg);
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResult<?> handleBindException(BindException e) {
        log.warn("[BindException] {}", e.getMessage());
        List<ObjectError> errors = e.getBindingResult().getAllErrors();
        ObjectError objectError = ValidatorUtil.getPermanentError(errors);
        String msg = objectError != null ? objectError.getDefaultMessage() : MessageUtils.getMessage(ApiError.HTTP_BAD_REQUEST);
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<?> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("[MissingServletRequestParameterException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ApiResult<?> handleMissingHeader(MissingRequestHeaderException e) {
        log.warn("[MissingRequestHeaderException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[HttpRequestMethodNotSupportedException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("[HttpMessageNotReadableException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(),
                ApiError.HTTP_BAD_REQUEST.getMsg() + ":" + e.getMessage());
    }

    // ===================== 数据库与系统异常 ===================== //

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResult<?> handleDuplicateKey(DuplicateKeyException e) {
        log.error("[DuplicateKeyException]", e);
        String msg = e.getMessage();
        if (msg != null && msg.contains("Duplicate entry") && msg.contains("for key")) {
            String duplicateKey = msg.substring(msg.indexOf("Duplicate entry") + 15, msg.indexOf("for key")).trim();
            return buildResult(ApiError.BILL_DATA_DUPLICATE.getCode(),
                    CharSequenceUtil.format("数据【{}】重复，请修改后再提交", duplicateKey));
        }
        return buildResult(ApiError.BILL_DATA_DUPLICATE);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<?> handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException]", e);
        if (e.getMessage() != null && e.getMessage().contains("value too long")) {
            return buildResult(ApiError.COMMON_PARAM_CONTENT_TOO_LONG);
        }
        return buildResult(ApiError.HTTP_UNKNOWN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<?> handleMaxUpload(MaxUploadSizeExceededException e) {
        log.error("[MaxUploadSizeExceededException]", e);
        return buildResult(ApiError.FILE_TOO_LARGE, e.getMaxUploadSize());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiResult<?> handleIllegalState(IllegalStateException e) {
        log.error("[IllegalStateException]", e);
        if (e.getMessage() != null && e.getMessage().contains("No instances available for")) {
            return buildResult(ApiError.HTTP_SERVICE_UNAVAILABLE);
        }
        return buildResult(ApiError.HTTP_UNKNOWN);
    }

    @ExceptionHandler(IllegalMonitorStateException.class)
    public ApiResult<?> handleIllegalMonitor(IllegalMonitorStateException e) {
        log.error("[IllegalMonitorStateException]", e);
        if (e.getMessage() != null && e.getMessage().contains("attempt to unlock lock")) {
            return buildResult(ApiError.BILL_DATA_LOCKED);
        }
        return buildResult(ApiError.HTTP_UNKNOWN);
    }

    @ExceptionHandler(MappingException.class)
    public ApiResult<?> handleMappingException(MappingException e) {
        log.error("[MappingException]", e);
        return buildResult(ApiError.COMMON_COPY_ERROR);
    }

    @ExceptionHandler(ClientException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleClientException(ClientException e) {
        log.error("[ClientException]", e);
        return buildResult(ApiError.HTTP_SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(NullPointerException.class)
    public ApiResult<?> handleNullPointer(NullPointerException e) {
        log.error("[NullPointerException]", e);
        return buildResult(ApiError.HTTP_UNKNOWN);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleGenericException(Exception e) {
        log.error("[UnknownException] {}", e.getMessage(), e);
        return buildResult(ApiError.HTTP_UNKNOWN.getCode(),
                MessageUtils.getMessage(ApiError.HTTP_UNKNOWN) + "：" + e.getMessage());
    }

    /** 客户端主动断开连接 */
    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    protected ApiResult<?> handleClientAbort(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
        log.warn("[ClientAbortException] 请求中断: {}", ex.getMessage());
        // 客户端已断开连接，不能返回内容
        return null;
    }

    // ===================== 工具方法 ===================== //

    /** 构造返回结果（自动国际化） */
    private ApiResult<?> buildResult(ApiError error, Object... args) {
        return buildResult(error.getCode(), MessageUtils.getMessage(error, args), null);
    }

    private ApiResult<?> buildResult(Integer code, String msg) {
        return buildResult(code, msg, null);
    }

    private ApiResult<Object> buildResult(Integer code, String msg, Object data) {
        ApiResult<Object> result = new ApiResult<>();
        result.setCode(code);
        result.setMsg(msg);
        if (data != null) {
            result.setData(data);
        }
        return result;
    }

    /** 设置 HTTP 状态（401 → UNAUTHORIZED, 默认200） */
    private void setHttpStatus(HttpServletResponse response, Integer code) {
        if (Objects.equals(code, ApiError.HTTP_UNAUTHORIZED.getCode()) ||
                Objects.equals(code, ApiError.HTTP_FORBIDDEN.getCode())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}