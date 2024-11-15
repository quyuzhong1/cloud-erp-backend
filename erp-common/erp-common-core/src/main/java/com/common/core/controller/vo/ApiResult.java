package com.common.core.controller.vo;



import java.io.Serializable;
import java.util.Objects;

import org.slf4j.MDC;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据结果返回的封装
 */
@Data
@NoArgsConstructor
public class ApiResult<T>  implements Serializable {

    /**
     * 响应消息
     */

    private String msg;


    /**
     * 响应代码
     */
    private Integer code;

    /**
     * 分布式链路id
     */
    private String traceId = MDC.get("traceId");

    /**
     * 请求或响应body
     */
    protected T data;
    

    /**
     * 是否成功
     *
     * @return
     */
    public boolean isSuccess() {
        return Objects.equals(code, 200);
    }


    /**
     * 构造函数
     *
     * @param error ApiError
     */
    public ApiResult(ApiError error) {
        this.code = error.code;
        this.msg = error.msg;
    }

    /**
     * 构造函数
     */
    public ApiResult(Integer code) {
        this.code = code;
    }

    /**
     * 构造函数
     *
     * @param error ServiceException
     */
    public ApiResult(ServiceException error) {
        this.code = error.getCode();
        this.msg = error.getMsg();
    }

    public ApiResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ApiResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static ApiResult error(ApiError error) {
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(error.code);
        apiResult.setMsg(error.msg);
        return apiResult;
    }

    public static <T> ApiResult<T> error(String msg) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(500);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static <T> ApiResult<T> error(String msg, T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(500);
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }


    public static <T> ApiResult<T> error(Integer code, String msg) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(code);
        apiResult.setMsg(msg);
        return apiResult;
    }

    /**
     * 成功时候的调用
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(200, "操作成功");
    }

    /**
     * 成功时候的调用
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "操作成功", data);
    }
    /**
     * 成功时候的调用
     */
    public static <T> ApiResult<T> success(String msg,T data) {
        return new ApiResult<>(200, msg, data);
    }
    /**
     * 成功时候的调用
     */
    public static <T> ApiResult<T> successMsg(String msg) {
        return new ApiResult<>(200, msg);
    }
}