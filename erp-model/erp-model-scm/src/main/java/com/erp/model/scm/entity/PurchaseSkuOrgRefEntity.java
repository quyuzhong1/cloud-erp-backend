package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * SKU与采购组织关系
 * </p>
 *
 * @author zdy
 * @since 2025-05-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("purchase_sku_org_ref")
public class PurchaseSkuOrgRefEntity extends BaseEntity<PurchaseSkuOrgRefEntity> {

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
    * sku 表id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku no
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String REMARK = "remark";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}