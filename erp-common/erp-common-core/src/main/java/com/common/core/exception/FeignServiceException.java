package com.common.core.exception;

import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import lombok.Data;

/**
 * @author Lambda
 * @Classname FeignServiceException
 * @Description TODO
 * @Date 2023-05-06 11:50
 * @Created by yl
 */
@Data
public class FeignServiceException extends RuntimeException {
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
        // 加上super，否则会显示null
        super(apiResult.getMsg());
        this.code = apiResult.getCode();
        this.msg = apiResult.getMsg();
    }

    /**
     * 从枚举中获取参数
     *
     * @param apiError
     */
    public FeignServiceException(ApiError apiError) {
        // 加上super，否则会显示null
        super(apiError.msg);
        this.code = apiError.code;
        this.msg = apiError.msg;
    }

    /**
     * 从枚举中获取参数
     *
     * @param
     */
    public FeignServiceException(int code, String msg) {
        // 加上super，否则会显示null
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
