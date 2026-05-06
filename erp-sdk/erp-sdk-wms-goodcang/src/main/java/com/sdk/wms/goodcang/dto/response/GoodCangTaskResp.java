package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangTaskResp  implements Serializable {

    @JSONField(name = "doc_no")
    private String docNo;
    @JSONField(name = "error_message")
    private String errorMessage;
    @JSONField(name = "request_id")
    private String requestId;
    @JSONField(name = "status")
    private Integer status;
}
