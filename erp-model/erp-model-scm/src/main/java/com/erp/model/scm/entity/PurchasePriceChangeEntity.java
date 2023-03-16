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
 * 采购价变更表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_price_change")
public class PurchasePriceChangeEntity extends BaseEntity<PurchasePriceChangeEntity> {

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 变更原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 报价日期
     */
    @TableField("quoted_date")
    private LocalDate quotedDate;

    /**
     * 定价人id
     */
    @TableField("pricing_user_id")
    private String pricingUserId;

    /**
     * 定价人
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

    /**
     * 采购价目表id
     *
     * @author yl
     * @date 2023-03-16 16:49
     * @param null
     * @return
     */
    @TableField("purchase_price_id")
    private String purchasePriceId;


    @Override
    public Serializable pkVal() {
        return null;
    }

}
