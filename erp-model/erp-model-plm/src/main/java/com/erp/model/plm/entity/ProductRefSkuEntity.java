package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 产品关联sku信息表
 * </p>
 *
 * @author codex
 * @since 2026-04-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("product_ref_sku")
public class ProductRefSkuEntity extends BaseEntity<ProductRefSkuEntity> {

    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;

    /**
     * 当前/所属sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 关联sku id
     */
    @TableField("ref_sku_id")
    private String refSkuId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    public static final String PRODUCT_ID = "product_id";

    public static final String SKU_ID = "sku_id";

    public static final String REF_SKU_ID = "ref_sku_id";

    public static final String REMARK = "remark";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
