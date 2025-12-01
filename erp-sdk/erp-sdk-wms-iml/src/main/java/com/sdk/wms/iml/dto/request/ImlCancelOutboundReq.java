package com.sdk.wms.iml.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImlCancelOutboundReq {

    @JSONField(name = "platformOrderNo")
    private String platformOrderNo;
    @JSONField(name = "ecPlatformOrderNo")
    private String ecPlatformOrderNo;
    @JSONField(name = "orderNo")
    private String orderNo;
    @JSONField(name = "trackNumber")
    private String trackNumber;
    @JSONField(name = "cause")
    private String cause;
}
