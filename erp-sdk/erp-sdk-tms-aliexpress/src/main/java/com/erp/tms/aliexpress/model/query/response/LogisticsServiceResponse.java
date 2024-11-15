package com.erp.tms.aliexpress.model.query.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author zdy
 * @ClassName LogisticsServiceResponse
 * @date 2023年12月29日
 * @version: 1.0
 */
@Data
public class LogisticsServiceResponse {
    @JSONField(name = "request_id")
    private String requestId;
    @JSONField(name = "result_response")
    private ServiceResponse resultResponse;
    @JSONField(name = "error_desc")
    private String errorDesc;
}
