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
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

import javax.validation.ConstraintViolationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname: GlobalExceptionHandler
 * @CreateTime: 2023-04-13  19:34
 * @Author: zhangchunlin
 */
@Slf4j
@RestControllerAdvice(basePackages = {
        "com.erp.server"
})
public class GlobalExceptionHandler {
    private static final Pattern MYSQL_DUPLICATE_PATTERN = Pattern.compile("Duplicate entry '([^']*)' for key");
    private static final Pattern PG_DUPLICATE_PATTERN = Pattern.compile("Key \\(([^)]*)\\)=\\(([^)]*)\\) already exists");
    private static final Pattern PG_NOT_NULL_PATTERN = Pattern.compile("null value in column \"([^\"]*)\"");

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

    @ExceptionHandler(ServletRequestBindingException.class)
    public ApiResult<?> handleServletRequestBinding(ServletRequestBindingException e) {
        log.warn("[ServletRequestBindingException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<?> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .orElse(MessageUtils.getMessage(ApiError.HTTP_BAD_REQUEST));
        log.warn("[ConstraintViolationException] {}", msg);
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResult<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = CharSequenceUtil.format("参数【{}】类型错误，请检查请求参数", e.getName());
        log.warn("[MethodArgumentTypeMismatchException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[HttpRequestMethodNotSupportedException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResult<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("[HttpMediaTypeNotSupportedException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ApiResult<?> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
        log.warn("[HttpMediaTypeNotAcceptableException] {}", e.getMessage());
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(), "请求的响应媒体类型不支持");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("[HttpMessageNotReadableException] {}", e.getMessage());
        log.error("[HttpMessageNotReadableException]异常信息", e);
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(), "请求体格式错误，请检查字段类型和JSON格式");
    }

    // ===================== 数据库与系统异常 ===================== //

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResult<?> handleDuplicateKey(DuplicateKeyException e) {
        log.error("[DuplicateKeyException]", e);
        String msg = resolveMostSpecificMessage(e);
        Matcher mysqlMatcher = MYSQL_DUPLICATE_PATTERN.matcher(CharSequenceUtil.blankToDefault(msg, ""));
        if (mysqlMatcher.find()) {
            return buildResult(ApiError.BILL_DATA_DUPLICATE.getCode(),
                    CharSequenceUtil.format("数据【{}】重复，请修改后再提交", mysqlMatcher.group(1)));
        }
        Matcher pgMatcher = PG_DUPLICATE_PATTERN.matcher(CharSequenceUtil.blankToDefault(msg, ""));
        if (pgMatcher.find()) {
            return buildResult(ApiError.BILL_DATA_DUPLICATE.getCode(),
                    CharSequenceUtil.format("数据【{}】重复，请修改后再提交", pgMatcher.group(2)));
        }
        return buildResult(ApiError.BILL_DATA_DUPLICATE);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<?> handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException]", e);
        String msg = resolveMostSpecificMessage(e);
        String lowerMsg = CharSequenceUtil.blankToDefault(msg, "").toLowerCase();
        if (lowerMsg.contains("duplicate key")) {
            return handleDuplicateKey(new DuplicateKeyException(msg, e));
        }
        if (lowerMsg.contains("value too long")) {
            return buildResult(ApiError.COMMON_PARAM_CONTENT_TOO_LONG);
        }
        Matcher notNullMatcher = PG_NOT_NULL_PATTERN.matcher(CharSequenceUtil.blankToDefault(msg, ""));
        if (notNullMatcher.find()) {
            return buildResult(ApiError.COMMON_PARAM_REQUIRED, notNullMatcher.group(1));
        }
        if (lowerMsg.contains("violates foreign key constraint")) {
            return buildResult(ApiError.BILL_IN_USE_DELETE_FORBIDDEN);
        }
        return buildResultWithTrace(ApiError.HTTP_UNKNOWN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<?> handleMaxUpload(MaxUploadSizeExceededException e) {
        log.error("[MaxUploadSizeExceededException]", e);
        return buildResult(ApiError.FILE_TOO_LARGE, e.getMaxUploadSize());
    }

    @ExceptionHandler(MultipartException.class)
    public ApiResult<?> handleMultipartException(MultipartException e) {
        log.error("[MultipartException]", e);
        return buildResultWithTrace(ApiError.FILE_UPLOAD_FAILED);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiResult<?> handleIllegalState(IllegalStateException e) {
        log.error("[IllegalStateException]", e);
        if (e.getMessage() != null && e.getMessage().contains("No instances available for")) {
            return buildResult(ApiError.HTTP_SERVICE_UNAVAILABLE);
        }
        return buildResultWithTrace(ApiError.HTTP_UNKNOWN);
    }

    @ExceptionHandler(IllegalMonitorStateException.class)
    public ApiResult<?> handleIllegalMonitor(IllegalMonitorStateException e) {
        log.error("[IllegalMonitorStateException]", e);
        if (e.getMessage() != null && e.getMessage().contains("attempt to unlock lock")) {
            return buildResult(ApiError.BILL_DATA_LOCKED);
        }
        return buildResultWithTrace(ApiError.HTTP_UNKNOWN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[IllegalArgumentException] {}", e.getMessage(), e);
        String msg = CharSequenceUtil.isNotBlank(e.getMessage())
                ? e.getMessage()
                : MessageUtils.getMessage(ApiError.HTTP_BAD_REQUEST);
        return buildResult(ApiError.HTTP_BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(MappingException.class)
    public ApiResult<?> handleMappingException(MappingException e) {
        log.error("[MappingException]", e);
        return buildResultWithTrace(ApiError.COMMON_COPY_ERROR);
    }

    @ExceptionHandler(ClientException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleClientException(ClientException e) {
        log.error("[ClientException]", e);
        return buildResult(ApiError.HTTP_SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(NullPointerException.class)
    public ApiResult<?> handleNullPointer(NullPointerException e, HttpServletRequest request) {
        String traceId = resolveTraceId();
        log.error("[NullPointerException] traceId={}, {}", traceId, resolveRequestInfo(request), e);
        return buildResultWithTrace(ApiError.HTTP_UNKNOWN, traceId);
    }

    /** 客户端主动断开连接 */
    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    protected ApiResult<?> handleClientAbort(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
        log.warn("[ClientAbortException] 请求中断: {}", ex.getMessage());
        // 客户端已断开连接，不能返回内容
        return null;
    }

    /** RuntimeException 兜底处理：日志记录明细，前端返回通用提示，避免泄露内部异常信息 */
    @ExceptionHandler(RuntimeException.class)
    public ApiResult<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String traceId = resolveTraceId();
        log.error("[RuntimeException] traceId={}, {} {}", traceId, resolveRequestInfo(request), e.getMessage(), e);
        return buildResultWithTrace(ApiError.HTTP_UNKNOWN, traceId);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleGenericException(Exception e, HttpServletRequest request) {
        String traceId = resolveTraceId();
        log.error("[UnknownException] traceId={}, {} {}", traceId, resolveRequestInfo(request), e.getMessage(), e);
        return buildResultWithTrace(ApiError.HTTP_UNKNOWN, traceId);
    }

    // ===================== 工具方法 ===================== //

    /** 构造返回结果（自动国际化） */
    private ApiResult<?> buildResult(ApiError error, Object... args) {
        return buildResult(error.getCode(), MessageUtils.getMessage(error, args), null);
    }

    /** 系统类异常返回 traceId，方便实施/开发按错误编号定位日志。 */
    private ApiResult<?> buildResultWithTrace(ApiError error, Object... args) {
        return buildResultWithTrace(error, resolveTraceId(), args);
    }

    private ApiResult<?> buildResultWithTrace(ApiError error, String traceId, Object... args) {
        String msg = MessageUtils.getMessage(error, args);
        return buildResult(error.getCode(), appendTraceId(msg, traceId), null);
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

    private String resolveMostSpecificMessage(Throwable e) {
        if (e instanceof org.springframework.core.NestedRuntimeException) {
            Throwable cause = ((org.springframework.core.NestedRuntimeException) e).getMostSpecificCause();
            if (cause != null && CharSequenceUtil.isNotBlank(cause.getMessage())) {
                return cause.getMessage();
            }
        }
        return e == null ? "" : e.getMessage();
    }

    private String resolveRequestInfo(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        return CharSequenceUtil.format("method={}, uri={}", request.getMethod(), request.getRequestURI());
    }

    private String resolveTraceId() {
        String traceId = TraceContext.traceId();
        return CharSequenceUtil.isNotBlank(traceId) ? traceId : UUID.randomUUID().toString().replace("-", "");
    }

    private String appendTraceId(String msg, String traceId) {
        return CharSequenceUtil.format("{}，错误编号：{}", CharSequenceUtil.blankToDefault(msg, ApiError.HTTP_UNKNOWN.getMsg()), traceId);
    }

    /** 设置 HTTP 状态码（401 → UNAUTHORIZED, 403 → FORBIDDEN, 默认200） */
    private void setHttpStatus(HttpServletResponse response, Integer code) {
        if (Objects.equals(code, ApiError.HTTP_UNAUTHORIZED.getCode())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401: 未认证
        } else if (Objects.equals(code, ApiError.HTTP_FORBIDDEN.getCode())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403: 无权限
        } else {
            response.setStatus(HttpServletResponse.SC_OK);  // 200: 默认
        }
    }
}