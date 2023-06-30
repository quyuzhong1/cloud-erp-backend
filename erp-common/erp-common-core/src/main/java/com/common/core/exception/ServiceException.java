package com.common.core.exception;

import cn.hutool.core.util.StrUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
     * @param apiResult
     */
    public ServiceException(ApiResult apiResult) {
        // 加上super，否则会显示null
        super(apiResult.getMsg());
        this.code = apiResult.getCode();
        this.msg = apiResult.getMsg();
        this.data = apiResult.getData();
    }


    /**
     * 从枚举中获取参数
     *
     * @param apiError
     */
    public ServiceException(ApiError apiError) {
        // 加上super，否则会显示null
        super(apiError.msg);
        this.code = apiError.code;
        this.msg = apiError.msg;
    }

    /**
     * 从枚举中获取参数
     *
     * @param apiError
     * @param args
     */
    public ServiceException(ApiError apiError,Object... args) {
        // 加上super，否则会显示null
        super(StrUtil.format(apiError.msg,args) );
        this.code = apiError.code;
        this.msg = StrUtil.format(apiError.msg,args) ;
    }

    /**
     * 从枚举中获取参数
     *
     * @param
     */
    public ServiceException(Integer code, String msg) {
        // 加上super，否则会显示null
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    /**
     * 统一错误码，错误描述外部传入
     *
     * @param
     */
    public ServiceException(String msg) {
        // 加上super，否则会显示null
        super(msg);
        this.code = ApiError.Default.code;
        this.msg = msg;
    }


}
