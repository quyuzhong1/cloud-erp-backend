package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:23
 */
@Data
@NoArgsConstructor
public class DmpRefundInfoDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 退款单号
     */
    private String refundId;

    /**
     * 平台订单编号
     */
    private String platformOrderId;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * SKU
     */
    private String skuNo;

    /**
     * 退货金额
     */
    private BigDecimal refundAmount;


    /**
     * 退款金额[RMB-实时]
     */
    private BigDecimal cnyRealTimeAmount;

    /**
     * 退款金额[RMB-实时]
     */
    private BigDecimal cnySettleAmount;

    /**
     * 退款数量
     */
    private BigDecimal refundNum;

    /**
     * 退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
     */
    private Integer refundStatus;

    /**
     * 退款状态名称
     */
    private String refundStatusName;

    /**
     * 退款时间
     */
    private Date refundTime;

    /**
     * 订单状态
     */
    private Integer platformOrderStatus;

}
