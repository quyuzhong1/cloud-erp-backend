package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 报价日期
     */
    @TableField("quoted_date")
    private Date quotedDate;

    /**
     * 报价人id
     */
    @TableField("make_price_user_id")
    private String makePriceUserId;

    /**
     * 报价人
     */
    @TableField("make_price_user_name")
    private String makePriceUserName;

    /**
     * 采购组织
     */
    @TableField("purch_org_id")
    private String purchOrgId;

    /**
     * 采购组织名
     */
    @TableField("purch_org_name")
    private String purchOrgName;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String AUDIT_STATUS = "audit_status";

    public static final String CODE = "code";

    public static final String QUOTED_DATE = "quoted_date";

    public static final String MAKE_PRICE_USER_ID = "make_price_user_id";

    public static final String MAKE_PRICE_USER_NAME = "make_price_user_name";

    public static final String PURCH_ORG_ID = "purch_org_id";

    public static final String PURCH_ORG_NAME = "purch_org_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
