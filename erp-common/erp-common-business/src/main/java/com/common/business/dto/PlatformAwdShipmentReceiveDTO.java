package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FBA货件DTO 所有平台(签收明细)通用数据，转换为此类后发送mq统一消费处理
 *
 * @author Jim
 * @date 2023/11/1
 **/
@Data
@NoArgsConstructor
public class PlatformAwdShipmentReceiveDTO {
    /**
     * 亚马逊FBA货件单号
     */
    private String fbaShipmentId;
    /**
     * FNSKU
     */
    private String fnSku;
    /**
     * msku
     */
    private String msku;
    /**
     * 卖家sku
     */
    private String sellerSku;
    /**
     * 申报数量
     */
    private Integer declareQty;
    /**
     * 发货数量
     */
    private Integer deliveryQty;
    /**
     * 收货数量
     */
    private Integer receiveQty;
    /**
     * 最新签收日期
     */
    private LocalDateTime receiveDate;

    /**
     * 单箱数量
     */
    private String perBoxQty;
    /**
     * 箱子数量
     */
    private String boxQty;
    /**
     * 箱子长
     */
    private BigDecimal packageLength;
    /**
     * 箱子宽
     */
    private BigDecimal packageWidth;
    /**
     * 箱子高
     */
    private BigDecimal packageHeight;
    /**
     * 箱子尺寸单位
     */
    private String packageUnit;
    /**
     * 箱子重量
     */
    private BigDecimal packageWeight;
    /**
     * 箱子重量单位
     */
    private String packageWeightUnit;

    public Integer calculateDiffQty(){
        return this.deliveryQty - this.receiveQty;
    }

    // 合并方法
    public PlatformAwdShipmentReceiveDTO merge(PlatformAwdShipmentReceiveDTO other) {
        this.setReceiveQty(this.getReceiveQty() + other.getReceiveQty());
        this.setReceiveDate(this.getReceiveDate().isAfter(other.getReceiveDate()) ? this.getReceiveDate() : other.getReceiveDate());
        return this;
    }
}
