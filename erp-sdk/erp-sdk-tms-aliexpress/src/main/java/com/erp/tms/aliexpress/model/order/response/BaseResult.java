package com.erp.tms.aliexpress.model.order.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName BaseResult
 * @date 2023年11月22日
 * @version: 1.0
 */
@Data
public class BaseResult implements Serializable {
    /**
     *
     */
    @JSONField(name = "request_id")
    private String requestId;
    /**
     *
     */
    @JSONField(name = "error_response")
    private ErrorResponse errorResponse;
    /**
     *
     */
    @JSONField(name = "result")
    private String result;
    /**
     * 错误码
     */
    @JSONField(name = "error_code")
    private String errorCode;
    /**
     * 错误描述
     */
    @JSONField(name = "error_msg")
    private String errorMsg;
    /**
     * 请求结果
     */
    @JSONField(name = "success")
    private Boolean success;
    /**
     * 详情
     */
    @JSONField(name = "data")
    private String data;
}
