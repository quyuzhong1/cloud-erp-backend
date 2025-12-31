package com.common.core.exception;


import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.MessageUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lambda
 * @Classname FeignServiceException

 * @Date 2023-05-06 11:50
 * @Created by yl
 */
@Data
@Slf4j
public class FeignServiceException extends Exception {
    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String msg;

    /**
     * 从结果初始化
     *
     * @param apiResult
     */
    public FeignServiceException(ApiResult apiResult) {
        super(apiResult != null ? apiResult.getMsg() : ApiError.HTTP_UNKNOWN.getMsg());
        this.code = apiResult != null ? apiResult.getCode() : ApiError.HTTP_UNKNOWN.getCode();
        this.msg = apiResult != null ? apiResult.getMsg() : ApiError.HTTP_UNKNOWN.getMsg();
        log.warn("[FeignServiceException] code={}, msg={}", code, msg);
    }

    /**
     * 从 ApiError 枚举初始化（支持国际化）
     */
    public FeignServiceException(ApiError apiError, Object... args) {
        super(resolveMessage(apiError, args));
        this.code = apiError != null ? apiError.getCode() : ApiError.HTTP_UNKNOWN.getCode();
        this.msg = resolveMessage(apiError, args);
        log.warn("[FeignServiceException] code={}, msg={}", code, msg);
    }

    /** 国际化解析（带回退机制） */
    private static String resolveMessage(ApiError apiError, Object... args) {
        if (apiError == null) {
            return ApiError.HTTP_UNKNOWN.getMsg();
        }
        try {
            String message = MessageUtils.getMessage(apiError, args);
            return message != null ? message : apiError.getMsg();
        } catch (Exception e) {
            log.warn("[FeignServiceException] 国际化解析失败，使用默认文案：{}", apiError.name());
            return apiError.getMsg();
        }
    }
}
