package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 订单DTO 所有平台(B2C销售订单物流信)通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 * @since 2023-10-09
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformOrderLogisticsDTO implements Serializable {

    /**
     * 物流单号
     */
    private String code;
    /**
     * 买家自选物流名称
     */
    private String name;
    /**
     * 物流渠道id
     */
    private String logisticsChannelId;
    /**
     * 物流渠道名称
     */
    private String logisticsChannelName;
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    /**
     * 预估运费
     */
    private BigDecimal estimatedShippingCost;
    /**
     * 预估运费币别
     */
    private String estimatedShippingCurrency;
    /**
     * 实际运费
     */
    private BigDecimal actualShippingCost;
    /**
     * 实际运费币别
     */
    private String actualShippingCurrency;
    /**
     * 包装重量
     */
    private BigDecimal weight;
    /**
     * 包装辅料skuId
     */
    private String accessoriesSkuId;
    /**
     * 包装辅料sku编码
     */
    private String accessoriesSkuNo;
    /**
     * 包装辅料数量
     */
    private Integer accessoriesQty;
    /**
     * 包装辅料净重
     */
    private BigDecimal accessoriesNw;
    /**
     * 包装辅料费
     */
    private BigDecimal accessoriesCost;
    /**
     * 包装辅料费币别
     */
    private String accessoriesCostCurrency;
    /**
     * 长
     */
    private BigDecimal length;
    /**
     * 宽
     */
    private BigDecimal width;
    /**
     * 高
     */
    private BigDecimal height;
    /**
     * 物流类型
     */
    private String logisticType;
    /**
     * 买家自选物流
     */
    private String buyerSelectedLogistics;
}