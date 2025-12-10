package com.sdk.wms.tongyou.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TongYouCancelOutboundReq {
    /**
     * 三方仓发货单号
     */
    @JSONField(name = "erpOrderCode")
    private String erpOrderCode;
}
