package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 订单DTO 所有平台(订单财务信息)通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 * @since 2023-10-09
 **/
@Data
@AllArgsConstructor
public class PlatformOrderFinanceDTO implements Serializable {
    /**
     * 币别
     */
    private String currency;
    /**
     * 运费收入
     */
    private BigDecimal shippingCost;
    /**
     * 商品成本
     */
    private BigDecimal itemCost;
    /**
     * 物流成本
     */
    private BigDecimal logisticsCost;

    /**
     * 包装辅料费
     */
    private BigDecimal accessoriesCost;

    /**
     * 平台费率
     */
    private BigDecimal platformRate;

    /**
     * vat 费率
     */
    private BigDecimal vatRate;

    /**
     * 转账费率
     */
    private BigDecimal transferRate;

    /**
     * 平台费类型
     */
    private String platformCostType;

    /**
     * 转账费类型
     */
    private String transferCostType;

    /**
     * VAT税费类型
     */
    private String vatCostType;


}