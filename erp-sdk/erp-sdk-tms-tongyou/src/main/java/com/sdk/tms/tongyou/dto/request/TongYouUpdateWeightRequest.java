package com.sdk.tms.tongyou.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author liuruipeng
 */
@Data
@Builder
@ToString
public class TongYouUpdateWeightRequest {

    //订单号
    @NotNull(message = "订单编号不能为空")
    private String orderNo;

    //追踪条码
    private String trackNo;

    //预报重量
    private BigDecimal weight;

}
