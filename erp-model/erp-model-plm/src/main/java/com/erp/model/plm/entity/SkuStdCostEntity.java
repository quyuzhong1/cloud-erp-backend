package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * sku标准成本表
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sku_std_cost")
public class SkuStdCostEntity extends BaseEntity<SkuStdCostEntity> {

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;
    /**
     * sku编号
     */
    @TableField("sku_no")
    private String skuNo;
    /**
     * 是否组合装
     */
    @TableField("is_comb")
    private Boolean isComb;
    /**
     * 最新出库时间
     */
    @TableField("last_outstock_date")
    private LocalDate lastOutstockDate;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String IS_COMB = "is_comb";

    public static final String LAST_OUTSTOCK_DATE = "last_outstock_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}