package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 供应商拜访物料信息表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_supplier_visit_sku")
public class ScmSupplierVisitSkuEntity extends BaseEntity<ScmSupplierVisitSkuEntity> {

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * sku 表id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 供应商拜访表id
     */
    @TableField("supplier_visit_id")
    private String supplierVisitId;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String SUPPLIER_VISIT_ID = "supplier_visit_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
