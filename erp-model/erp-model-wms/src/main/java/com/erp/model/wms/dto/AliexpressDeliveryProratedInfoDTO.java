package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 速卖通菜鸟仓发货单分摊信息
 */
@Data
@NoArgsConstructor
public class AliexpressDeliveryProratedInfoDTO implements Serializable {

    /**
     * 物流单号
     */
    private String trackNo;
    /**
     * 下发到仓时间戳
     */
    private LocalDateTime deliveryWarehouseTime;
    /**
     * 订单状态
     */
    private String orderStatus;
    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * 唯一ID
     * 速卖通=中台明细ID
     */
    private String uniqueId;

    /**
     * 订单明细来源单价
     */
    private BigDecimal price;

    /**
     * 平台skuId
     */
    private String platformSkuId;

    /**
     * 平台Spu
     */
    private String platformSpuNo;

    /**
     * 发货数量
     */
    private Integer qty;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 实际支付币别
     */
    private String payCurrency;

    /**
     * 平台订单明细状态
     */
    private String orderDetailPlatformStatus;

    /**
     * 订单税后支付金额
     */
    private BigDecimal afterTaxAmount;

    /**
     * 订单总金额
     */
    private BigDecimal orderAmount;

    /**
     * 计算后的发货单单税后支付金额
     */
    private BigDecimal deliveryAfterTaxAmount;

    /**
     * 计算后的发货单明细支付金额
     */
    private BigDecimal proratedAfterTaxAmount;

    /**
     * 计算后的发货单明细支付单价
     */
    private BigDecimal proratedAfterTaxUnitPrice;

    /**
     * 计算后的发货明细单价
     */
    private BigDecimal proratedUnitPrice;

    /**
     * 计算后的发货明细总价
     */
    private BigDecimal proratedAmount;

    /**
     * 来源订单明细总商品价格
     */
    private BigDecimal orderDetailAmount;

    /**
     * 订单明细明细ID
     */
    private String platformOrderDetailId;

    /**
     * 订单明细数量
     */
    private Integer orderDetailQty;



    /**
     * 发货单分摊金额
     */
    public static BigDecimal calculateDeliveryProratedAmount(BigDecimal targetProratedAmount, BigDecimal actualAmount, BigDecimal detailActualAmount) {
        if (actualAmount.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        // 当前发货税后金额
        return targetProratedAmount.multiply(detailActualAmount)
                .divide(actualAmount, 4, RoundingMode.DOWN);
    }

}