package com.erp.tms.aliexpress.model.order.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ErrorResponse
 * @date 2023年11月22日
 * @version: 1.0
 */
@Data
public class ErrorResponse implements Serializable {
    /**
     * {"error_response":{"code":"15","type":"ISP","sub_code":"P-088-0000-00-15-106","sub_msg":"批次包裹不存在或不能在包裹状态揽收后在追加大包","msg":"Remote service error","request_id":"2141244f17072121132917360"}}
     */
    private String type;
    private String code;
    @JSONField(name = "sub_code")
    private String subCode;
    private String msg;
    @JSONField(name = "sub_msg")
    private String subMsg;
    @JSONField(name = "request_id")
    private String requestId;
}
