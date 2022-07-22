package com.cloud.erp.common.common;


import com.cloud.erp.common.common.exception.ServiceException;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据结果返回的封装
 */
@Data
@NoArgsConstructor
public class ApiResult<T> {

    /**
     * 响应消息
     */

    private String msg;


    /**
     * 响应代码
     */
    private Integer code;


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
        return code.equals(200);
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


    public static ApiResult error(ApiError error) {
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(error.code);
        apiResult.setMsg(error.msg);
        return apiResult;
    }

    public static ApiResult error(Integer code, String msg) {
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(code);
        apiResult.setMsg(msg);
        return apiResult;
    }


}