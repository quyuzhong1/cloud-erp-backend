package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品采购信息表请求参数
 * @TableName product_purchase
 * @Author Luo_WG
 * @Date 2022/9/23 12:14
 **/
@Data
@NoArgsConstructor
public class ProductPurchaseDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * sku表id
     */
    private String skuId;

    /**
     * EAN码
     */
    private String ean;

    /**
     * 计划首批下单量
     */
    private Long planOrderQty;

    /**
     * 首批下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date placeOrderTime;

    /**
     * 预计首批到货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planArrivalTime;

    /**
     * MOQ(最小起订量)
     */
    private Integer moq;

    /**
     * 交货周期(天)
     */
    private BigDecimal deliveryCycle;

    /**
     * 实际首批到货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date actualArrivalTime;

    /**
     * 首批到货状态：1.未到货 2.已到货 3.部分到货
     */
    private Integer arrivalState;

    /**
     * 采购员
     */
    private String purchaseUserId;

    /**
     * 一级供应商
     */
    private String mainSupplier;

    /**
     * 二级供应商
     */
    private String secondSupplier;

    /**
     * 单位表id
     */
    private String createUserId;

    /**
     * 单位表id
     */
    private String updateUserId;

    /**
     * 实际首批到货量
     */
    private Long actualArrivalQty;

    /**
     * 试产/产量
     */
    private Long trialProductionOutput;

    /**
     * 试产数量
     */
    private Long trialProductionQty;

    /**
     * 首批量产数量
     */
    private Long firstMassQty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}