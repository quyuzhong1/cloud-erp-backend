package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangOutboundResp extends CleanBaseDTO implements Serializable {

    //订单号
    @JSONField(name = "order_code")
    private String orderCode;

    //入库单状态
    @JSONField(name = "reference_no")
    private String referenceNo;

    //订单状态
    @JSONField(name = "order_status")
    private String orderStatus;

}
