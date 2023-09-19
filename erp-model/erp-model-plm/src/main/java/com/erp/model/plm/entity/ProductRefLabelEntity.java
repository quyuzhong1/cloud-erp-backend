package com.erp.model.plm.entity;

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
 * 产品便签关系表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("product_ref_label")
public class ProductRefLabelEntity extends BaseEntity<ProductRefLabelEntity> {

    /**
    * sku id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品id
    */
    @TableField("product_id")
    private String productId;
    /**
    * 标签id
    */
    @TableField("lable_id")
    private String lableId;


    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_ID = "product_id";

    public static final String LABLE_ID = "lable_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}