package com.sdk.oms.temu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemuShippingInfoReq extends TemuCommonDTO {

    /**
     * 平台订单号
     */
    private String parentOrderSn;
}
