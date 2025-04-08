package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 时效配置表
 * </p>
 *
 * @author liaohui
 * @since 2025-02-13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_expire_time")
public class CfgRuleExpireTimeEntity extends BaseEntity<CfgRuleExpireTimeEntity> {

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
     * 平台仓安全天数（天）
     */
    @TableField("platform_instock_days")
    private Integer platformInstockDays;

    /**
     * 海外入库天数（天）
     */
    @TableField("overseas_instock_days")
    private Integer overseasInstockDays;

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

    public static final String PLATFORM_INSTOCK_DAYS = "platform_instock_days";

    public static final String OVERSEAS_INSTOCK_DAYS = "overseas_instock_days";

    public static final String REF_ID = "ref_id";

    public static final String REF_TYPE = "ref_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
