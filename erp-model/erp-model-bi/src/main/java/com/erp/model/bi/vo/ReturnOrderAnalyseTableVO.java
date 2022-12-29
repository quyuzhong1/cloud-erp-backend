package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退货分析列表数据实体类
 * @Author Luo_WG
 * @Date 2022/12/16 14:47
 **/
@Data
@NoArgsConstructor
public class ReturnOrderAnalyseTableVO {
    /**
     * 分类：品类 店铺 平台 事业部
     */
    private String classify;

    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     * 销售额
     */
    private BigDecimal sales;

    /**
     * 退货订单数量
     */
    private Integer returnOrderQty;

    /**
     * 退货金额
     */
    private BigDecimal returnOrderAmount;

    /**
     * 退款订单数
     */
    private Integer refundOrderQty;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退货率
     */
    private BigDecimal refundOrderRate;

    /**
     * 退款率
     */
    private BigDecimal refundRate;

}
