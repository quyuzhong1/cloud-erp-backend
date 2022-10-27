package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品采购信息表（VO）
 * @Author Luo_WG
 * @Date 2022/9/23 12:14
 **/
@Data
@NoArgsConstructor
public class ProductPurchaseShowDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 产品sku表id
     */
    private String skuId;

    /**
     * 产品sku图片
     */
    private String imagesUrl;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * EAN码
     */
    private String ean;

    /**
     * 计划首批下单量
     */
    private Integer planOrderQty;

    /**
     * 首批下单时间
     */
    private Date placeOrderTime;

    /**
     * 预计首批到货时间
     */

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
     * 采购员
     */
    private String purchaseUserName;

    /**
     * 一级供应商
     */
    private String mainSupplier;

    /**
     * 二级供应商
     */
    private String secondSupplier;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 实际首批到货量
     */
    private Integer actualArrivalQty;

    private static final long serialVersionUID = 1L;
}