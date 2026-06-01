package com.common.core.exception;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.MessageUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
public class ServiceException extends RuntimeException {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String msg;

    /**
     * 异常返回的data
     */
    private Object data;

    /**
     * 从结果初始化
     *
     * @param apiResult API结果
     */
    public ServiceException(ApiResult apiResult) {
        // 加上super，否则会显示null
        super(apiResult.getMsg());
        this.code = apiResult.getCode();
        this.msg = apiResult.getMsg();
        this.data = apiResult.getData();
        log.error("[ServiceException] code={}, msg={}", code, msg);
    }


    /** 仅传入消息，使用默认错误码 */
    public ServiceException(String msg, Object... args) {
        this(ApiError.HTTP_UNKNOWN.getCode(), msg, args);
    }

    /** 传入原始异常，使用默认错误码 */
    public ServiceException(Throwable cause, String msg, Object... args) {
        super(CharSequenceUtil.format(msg, args), cause);
        this.code = ApiError.HTTP_UNKNOWN.getCode();
        this.msg = CharSequenceUtil.format(msg, args);
        this.data = null;
        log.error("[ServiceException] code={}, msg={}", code, this.msg, cause);
    }

    /**
     * 从枚举中获取参数
     *
     * @param apiError  错误信息
     * @param args      错误信息格式化 参数
     */
    public ServiceException(ApiError apiError,Object... args) {
        // 加上super，否则会显示null
        super(resolveMessage(apiError, args));
        this.code = apiError.getCode();
        this.msg = resolveMessage(apiError, args);
        log.error("[ServiceException] code={}, msg={}", code, msg);
    }

    /** 直接构建（非国际化消息） */
    public ServiceException(Integer code, String msg, Object... args) {
        super(CharSequenceUtil.format(msg, args));
        this.code = code != null ? code : ApiError.HTTP_UNKNOWN.getCode();
        this.msg = CharSequenceUtil.format(msg, args);
        this.data = null;
        log.error("[ServiceException] code={}, msg={}", code, this.msg);
    }

    // ====================== 静态快速抛出方法 ====================== //
    /**
     * 抛出 ServiceException 异常
     * @param msg    错误消息,支持格式化，如：XXX[{}]成功
     */
    public static void runError(String msg,Object... params) {
        throw new ServiceException(msg,params);
    }

    /**
     * 抛出 ServiceException 异常，按约定的ApiError指定类型
     * @param apiError  错误类型
     * @param params    错误信息 参数
     */
    public static void runError(ApiError apiError,Object... params) {
        throw new ServiceException(apiError, params);
    }

    /**
     * 抛出 ServiceException 异常
     * @param code              错误代号
     * @param msg    错误消息,支持格式化，如：XXX[{}]成功
     * @param params            错误信息 参数
     */
    public static void runError(Integer code, String msg,Object... params) {
        throw new ServiceException(code,CharSequenceUtil.format(msg,params));
    }

    /**
     * 抛出 ServiceException 异常，警告类型
     * @param formatedErrMsg    错误消息,支持格式化，如：XXX[{}]成功
     * @param params            错误信息 参数
     */
    public static void runWarn(String formatedErrMsg,Object... params) {
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        Integer code = ApiError.WARNING.getCode();
        throw new ServiceException(code,msg);
    }

    // ====================== 工具方法 ====================== //

    /**
     * 国际化消息解析（带回退机制）
     */
    private static String resolveMessage(ApiError apiError, Object... args) {
        if (apiError == null) {
            return ApiError.HTTP_UNKNOWN.getMsg();
        }
        try {
            String msg = MessageUtils.getMessage(apiError, args);
            return CharSequenceUtil.isNotBlank(msg) ? msg : apiError.getMsg();
        } catch (Exception e) {
            log.warn("[ServiceException] 国际化消息解析失败，使用默认文案：{}", apiError.name());
            return apiError.getMsg();
        }
    }
}


