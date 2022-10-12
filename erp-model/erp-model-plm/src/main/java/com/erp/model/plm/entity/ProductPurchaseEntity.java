package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品采购信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:05
 **/
@TableName(value ="product_purchase")
@Data
public class ProductPurchaseEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * EAN码
     */
    @TableField(value = "ean")
    private Integer ean;

    /**
     * 计划首批下单量
     */
    @TableField(value = "plan_order_qty")
    private Integer planOrderQty;

    /**
     * 首批下单时间
     */
    @TableField(value = "place_order_time")
    private Date placeOrderTime;

    /**
     * 预计首批到货时间
     */
    @TableField(value = "plan_arrival_time")
    private Date planArrivalTime;

    /**
     * MOQ(最小起订量)
     */
    @TableField(value = "moq")
    private Integer moq;

    /**
     * 交货周期(天)
     */
    @TableField(value = "delivery_cycle")
    private BigDecimal deliveryCycle;

    /**
     * 实际首批到货时间
     */
    @TableField(value = "actual_arrival_time")
    private Date actualArrivalTime;

    /**
     * 首批到货状态：1.未到货 2.已到货 3.部分到货
     */
    @TableField(value = "arrival_state")
    private Integer arrivalState;

    /**
     * 采购员
     */
    @TableField(value = "purchase_user_id")
    private Integer purchaseUserId;

    /**
     * 一级供应商
     */
    @TableField(value = "main_supplier")
    private String mainSupplier;

    /**
     * 二级供应商
     */
    @TableField(value = "second_supplier")
    private String secondSupplier;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    /**
     * 实际首批到货量
     */
    @TableField(value = "actual_arrival_qty")
    private Integer actualArrivalQty;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name")
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name")
    private String updateUserName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}