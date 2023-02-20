package com.common.core.exception;

import com.common.core.enums.ApiError;
import com.common.core.controller.vo.ApiResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
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
     * 从结果初始化
     *
     * @param apiResult
     */
    public ServiceException(ApiResult apiResult) {
        this.code = apiResult.getCode();
        this.msg = apiResult.getMsg();
    }

    /**
     * 从枚举中获取参数
     *
     * @param apiError
     */
    public ServiceException(ApiError apiError) {
        this.code = apiError.code;
        this.msg = apiError.msg;
    }

    /**
     * 从枚举中获取参数
     *
     * @param
     */
    public ServiceException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


}
