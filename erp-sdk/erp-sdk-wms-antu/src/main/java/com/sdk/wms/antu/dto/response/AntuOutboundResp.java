package com.sdk.wms.antu.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AntuOutboundResp extends CleanBaseDTO implements Serializable {

    //订单号
    @JSONField(name = "order_code")
    private String orderCode;

    //客户参考号
    @JSONField(name = "reference_no")
    private String referenceNo;

    //订单状态
    @JSONField(name = "order_status")
    private String orderStatus;

    //出库时间
    @JSONField(name = "date_shipping")
    private LocalDateTime outBoundTime;

    //跟踪号
    @JSONField(name = "tracking_no")
    private String trackNo;

    @JSONField(name = "abnormal_reason")
    private String abnormalReason;
}
