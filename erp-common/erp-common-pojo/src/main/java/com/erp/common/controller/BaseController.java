package com.erp.common.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import lombok.NoArgsConstructor;


/**
 * 基础控制器
 */

@NoArgsConstructor
public class BaseController {

    /**
     * 成功默认消息
     */
    private static final Integer CODE_SUCCESS = 200;
    private static final String MSG_SUCCESS = "操作成功！";

    /**
     * 失败默认消息
     */
    private static final Integer CODE_FAILURE = 1;
    private static final String MSG_FAILURE = "请求失败！";

    /**
     * 部分成功默认消息
     */
    private static final Integer CODE_PARTIAL_SUCCESS = 2;
    private static final String MSG_PARTIAL_SUCCESS = "部分成功！";


    /**
     * 完成消息构造
     *
     * @param code
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> message(Integer code, String message, T data) {
        ApiResult<T> response = new ApiResult<>();
        response.setCode(code);
        response.setMsg(message);
        if (data != null) {
            response.setData(data);
        }
        return response;
    }

    /**
     * 请求成功空数据
     *
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> success() {
        return message(CODE_SUCCESS, "请求成功！", null);
    }


    /**
     * 请求成功，通用代码
     *
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> success(String message, T data) {
        return message(CODE_SUCCESS, message, data);
    }


    /**
     * 请求成功，仅内容
     *
     * @param data
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> success(T data) {
        return message(CODE_SUCCESS, MSG_SUCCESS, data);
    }

    /**
     * 部分成功空数据
     *
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> partialSuccess() {
        return message(CODE_PARTIAL_SUCCESS, MSG_PARTIAL_SUCCESS, null);
    }


    /**
     * 部分成功，通用代码
     *
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> partialSuccess(String message, T data) {
        return message(CODE_PARTIAL_SUCCESS, message, data);
    }


    /**
     * 部分成功，仅内容
     *
     * @param data
     * @param <T>
     * @return
    */
    protected <T> ApiResult<T> partialSuccess(T data) {
        return message(CODE_PARTIAL_SUCCESS, MSG_PARTIAL_SUCCESS, data);
    }


    /**
     * 请求失败，完整构造
     *
     * @param code
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> failure(Integer code, String message, T data) {
        return message(code, message, data);
    }

    /**
     * 请求失败，消息和内容
     *
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> failure(String message, T data) {
        return message(CODE_FAILURE, message, data);
    }

    /**
     * 请求失败，消息
     *
     * @param message
     * @return
     */
    protected <T> ApiResult<T> failure(String message) {
        return message(CODE_FAILURE, message, null);
    }

    /**
     * 请求失败，仅内容
     *
     * @param data
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> failure(T data) {
        return message(CODE_FAILURE, MSG_FAILURE, data);
    }


    /**
     * 请求失败，仅内容
     *
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> failure() {
        return message(CODE_FAILURE, MSG_FAILURE, null);
    }


    /**
     * 请求失败，仅内容
     *
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> failure(ApiError error, T data) {
        return message(error.code, error.msg, data);
    }


    /**
     * 请求失败，仅内容
     *
     * @param ex
     * @param <T>
     * @return
     */
    protected <T> ApiResult<T> failure(ServiceException ex) {
        ApiResult<T> apiResult = message(ex.getCode(), ex.getMsg(), null);
        return apiResult;
    }
}
