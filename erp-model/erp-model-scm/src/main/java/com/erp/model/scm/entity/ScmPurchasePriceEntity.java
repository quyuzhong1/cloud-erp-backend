package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 采购价目表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_price")
public class ScmPurchasePriceEntity extends BaseEntity<ScmPurchasePriceEntity> {

    /**
     * 供应商表id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 报价日期
     */
    @TableField("quoted_date")
    private LocalDate quotedDate;

    /**
     * 报价人id
     */
    @TableField("pricing_user_id")
    private String pricingUserId;

    /**
     * 报价人
     */
    @TableField("pricing_user_name")
    private String pricingUserName;

    /**
     * 采购组织
     */
    @TableField("purchase_org_id")
    private String purchaseOrgId;

    /**
     * 采购组织名
     */
    @TableField("purchase_org_name")
    private String purchaseOrgName;



    @Override
    public Serializable pkVal() {
        return null;
    }

}
