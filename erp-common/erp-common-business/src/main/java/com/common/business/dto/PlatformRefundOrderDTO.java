package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台退款订单DTO
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformRefundOrderDTO extends UniqueDto {

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 平台退款单号
     */
    private String platformRefundNo;
    /**
     * 平台订单编号
     */
    private String platformOrderNo;
    /**
     * 备注
     */
    private String remark;
    /**
     * 平台
     */
    private String dictPlatform;
    /**
     * 币别
     */
    private String currency;
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 明细
     */
    private List<Detail> detailList;

    @Data
    @ToString
    public static class Detail {

        //商品SKU(第三方)
        private String platformSkuNo;

        //退款数量
        private Integer refundQty;
    }

}
