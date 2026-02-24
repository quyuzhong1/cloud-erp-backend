package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 产品bu信息关联表
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("product_ref_bu")
public class ProductRefBuEntity extends BaseEntity<ProductRefBuEntity> {

    /**
    * 产品Id
    */
    @TableField("product_id")
    private String productId;
    /**
    * buId
    */
    @TableField("bu_id")
    private String buId;

    @TableField(exist = false)
    private String buName;

    public static final String PRODUCT_ID = "product_id";

    public static final String BU_ID = "bu_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}