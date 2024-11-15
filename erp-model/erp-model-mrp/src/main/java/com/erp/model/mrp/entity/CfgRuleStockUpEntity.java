package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 备货（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_stock_up")
public class CfgRuleStockUpEntity extends BaseEntity<CfgRuleStockUpEntity> {

    /**
    * 采购审批天数（天）
    */
    @TableField("purchase_approve_days")
    private Integer purchaseApproveDays;
    /**
    * 生产周期天数（天）
    */
    @TableField("production_days")
    private Integer productionDays;
    /**
    * 供应商发货天数（天）
    */
    @TableField("supplier_delivery_days")
    private Integer supplierDeliveryDays;
    /**
    * 质检入库天数（天）
    */
    @TableField("qc_days")
    private Integer qcDays;
    /**
    * 采购频率天数（天）
    */
    @TableField("purchase_cycle_days")
    private Integer purchaseCycleDays;
    /**
    * 安全天数（天）
    */
    @TableField("safe_days")
    private Integer safeDays;
    /**
    * 入库天数（天）
    */
    @TableField("instock_days")
    private Integer instockDays;
    /**
    * 常规品备货系数
    */
    @TableField("stocking_ratio")
    private BigDecimal stockingRatio;
    /**
    * 新品备货系数
    */
    @TableField("new_stocking_ratio")
    private BigDecimal newStockingRatio;
    /**
    * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
    */
    @TableField("platform_type")
    private String platformType;
    /**
    * 关联id
    */
    @TableField("ref_id")
    private String refId;
    /**
    * 关联类型
    */
    @TableField("ref_type")
    private String refType;


    public static final String PURCHASE_APPROVE_DAYS = "purchase_approve_days";

    public static final String PRODUCTION_DAYS = "production_days";

    public static final String SUPPLIER_DELIVERY_DAYS = "supplier_delivery_days";

    public static final String QC_DAYS = "qc_days";

    public static final String PURCHASE_CYCLE_DAYS = "purchase_cycle_days";

    public static final String SAFE_DAYS = "safe_days";

    public static final String INSTOCK_DAYS = "instock_days";

    public static final String STOCKING_RATIO = "stocking_ratio";

    public static final String NEW_STOCKING_RATIO = "new_stocking_ratio";

    public static final String PLATFORM_TYPE = "platform_type";

    public static final String REF_ID = "ref_id";

    public static final String REF_TYPE = "ref_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}