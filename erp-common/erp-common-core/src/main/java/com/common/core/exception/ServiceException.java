package com.common.core.exception;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
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
        log.error(msg);
    }


    /**
     * 从枚举中获取参数
     *
     * @param apiError  API错误信息
     */
    public ServiceException(ApiError apiError) {
        // 加上super，否则会显示null
        super(apiError.msg);
        this.code = apiError.code;
        this.msg = apiError.msg;
        log.error(msg);
    }

    public ServiceException(String formatErrMsg,Object... args) {
        // 加上super，否则会显示null
        super(CharSequenceUtil.format(formatErrMsg,args) );
        this.code = ApiError.DEFAULT.code;
        this.msg = CharSequenceUtil.format(formatErrMsg,args) ;
        log.error(msg);
    }

    /**
     * 从枚举中获取参数
     *
     * @param apiError  错误信息
     * @param args      错误信息格式化 参数
     */
    public ServiceException(ApiError apiError,Object... args) {
        // 加上super，否则会显示null
        super(CharSequenceUtil.format(apiError.msg,args) );
        this.code = apiError.code;
        this.msg = CharSequenceUtil.format(apiError.msg,args) ;
        log.error(msg);
    }

    /**
     * 构建 异常信息
     * @param code  错误代号
     * @param msg   错误信息
     */
    public ServiceException(Integer code, String msg) {
        // 加上super，否则会显示null
        super(msg);
        this.code = code;
        this.msg = msg;
        log.error(msg);
    }

    /**
     * 构建 异常信息
     * @param msg   错误信息
     */
    public ServiceException(String msg) {
        // 加上super，否则会显示null
        super(msg);
        this.code = ApiError.DEFAULT.code;
        this.msg = msg;
        log.error(msg);
    }

    /**
     * 抛出 ServiceException 异常
     * @param msg    错误消息,支持格式化
     */
    public static void runError(String msg) {
        throw new ServiceException(msg);
    }
    /**
     * 抛出 ServiceException 异常
     * @param formatedErrMsg    错误消息,支持格式化，如：XXX[{}]成功
     */
    public static void runError(String formatedErrMsg,Object... params) {
        throw new ServiceException(formatedErrMsg,params);
    }

    /**
     * 抛出 ServiceException 异常
     * @param code  错误代号
     * @param msg   错误消息
     */
    public static void runError(Integer code,String msg) {
        throw new ServiceException(code,msg);
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
     * @param formatedErrMsg    错误消息,支持格式化，如：XXX[{}]成功
     * @param params            错误信息 参数
     */
    public static void runError(Integer code, String formatedErrMsg,Object... params) {
        throw new ServiceException(code,CharSequenceUtil.format(formatedErrMsg,params));
    }

    /**
     * 抛出 ServiceException 异常，警告类型
     * @param formatedErrMsg    错误消息,支持格式化，如：XXX[{}]成功
     * @param params            错误信息 参数
     */
    public static void runWarn(String formatedErrMsg,Object... params) {
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        Integer code = ApiError.WARNING.code;
        throw new ServiceException(code,msg);
    }

}


