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
 * 关联下单产品
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_ref_product")
public class MouldRefProductEntity extends BaseEntity<MouldRefProductEntity> {

    /**
    * 模具id
    */
    @TableField("mould_detail_id")
    private String mouldDetailId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * skuNo
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SUPPLIER_ID = "supplier_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}