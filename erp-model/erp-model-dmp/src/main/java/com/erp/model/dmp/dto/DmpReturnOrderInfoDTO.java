package com.erp.model.dmp.dto;

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
public class DmpReturnOrderInfoDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 退货单号
     */
    private String returnOrderId;

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
    private BigDecimal orderFee;

    /**
     * 退货金额[RMB-实时]
     */
    private BigDecimal cnyRealTimeAmount;

    /**
     * 退货金额[RMB-结算]
     */
    private BigDecimal cnySettleAmount;

    /**
     * 退货数量
     */
    private BigDecimal returnNum;

    /**
     * 状态：1待处理 2已退款 3已重发 4已完成 5已作废
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 退货时间
     */
    private Date returnCreateTime;

    /**
     * 原订单时间
     */
    private Date orderTime;

}
