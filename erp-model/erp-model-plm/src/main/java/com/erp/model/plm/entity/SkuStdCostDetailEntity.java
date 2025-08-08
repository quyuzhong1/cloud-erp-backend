package com.erp.model.plm.entity;

import java.math.BigDecimal;

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
 * sku标准成本明细表
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sku_std_cost_detail")
public class SkuStdCostDetailEntity extends BaseEntity<SkuStdCostDetailEntity> {

    /**
     * 主表ID:sku_std_cost
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 生效日期
     */
    @TableField("effective_date")
    private LocalDate effectiveDate;
    /**
     * 失效日期
     */
    @TableField("expire_date")
    private LocalDate expireDate;
    /**
     * 标准成本
     */
    @TableField("std_sale_price")
    private BigDecimal stdCostPrice;
    /**
     * 标准零售价(不含税)
     */
    @TableField("std_cost_price")
    private BigDecimal stdSalePrice;
    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * bom版本
     */
    @TableField("bom_version")
    private String bomVersion;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
     * 审核人ID
     */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;


    public static final String MAIN_ID = "main_id";

    public static final String EFFECTIVE_DATE = "effective_date";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String STD_COST_PIRCE = "std_cost_pirce";

    public static final String STD_SALE_PRICE = "std_sale_price";

    public static final String BOM_VERSION = "bom_version";

    public static final String REMARK = "remark";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}