package com.sdk.tms.yuntu.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author liuruipeng
 */
@Data
@Builder
public class YunTuUpdateWeightRequest {

    @NotNull(message = "订单号不能为空")
    private String orderNumber;

    //KG
    @NotNull(message = "重量不能为空")
    private BigDecimal weight;

}
