package com.cloud.erp.common.common;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据结果返回的封装
 */
@Data
@NoArgsConstructor
public class ApiRest<T> {

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
        return code.equals(0);
    }



    /**
     * 构造函数
     *
     * @param error ApiError
     */
    public ApiRest(ApiError error) {
        this.code = error.code;
        this.msg = error.msg;
    }

    /**
     * 构造函数
     *
     */
    public ApiRest(Integer code) {
        this.code = code;
    }
}