package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName BaseResponse
 * @description: TODO
 * @date 2024年02月06日
 * @version: 1.0
 */
@Data
public class BaseResponse implements Serializable {
    @JSONField(name = "response")
    private String response;
    @JSONField(name = "result")
    private String result;
    @JSONField(name = "request_id")
    private String requestId;
}
