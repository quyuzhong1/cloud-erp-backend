package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单DTO 所有平台(订单明细)通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 **/
@Data
@NoArgsConstructor
public class PlatformDeliveryDetailDTO {

    /**
     * skuId
     */
    private String skuId;
    /**
     * 产品sku编号
     */
    private String skuNo;

    /**
     * 平台sku编号
     */
    private String platformSkuNo;

    /**
     * 货品id
     */
    private String scItemId;

    /**
     * 平台skuId
     */
    private String platformSkuId;
    /**
     * 平台产品id
     */
    private String platformSpuNo;
    /**
     * 数量
     */
    private Integer qty;
    /**
     * ERP仓库id
     */
    private String warehouseId;
    /**
     * ERP仓库名称
     */
    private String warehouseName;
    /**
     * 平台仓库名称
     */
    private String platformWarehouseName;
    /**
     * 单价
     */
    private BigDecimal price;
    /**
     * ERP仓库组织id
     */
    private String warehouseOrgId;
    /**
     * ERP仓库组织名称
     */
    private String warehouseOrgName;
    /**
     *来源平台
     */
    private String sourcePlatform = "thirdPlatform";

    // 速卖通发货单明细信息
    /**
     * 币别(速卖通发货单明细来源单价币种)
     */
    private String currency;
    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 实际支付币别
     */
    private String payCurrency;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 折扣币别
     */
    private String discountCurrency;

    /**
     * 唯一ID:
     * 速卖通=中台明细ID
     */
    private String uniqueId;

    /**
     * 备注
     */
    private String remark = "";
}
