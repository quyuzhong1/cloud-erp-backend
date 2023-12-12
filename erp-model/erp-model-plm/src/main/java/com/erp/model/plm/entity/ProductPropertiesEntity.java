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
 * sku与配置字段关系表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("product_properties")
public class ProductPropertiesEntity extends BaseEntity<ProductPropertiesEntity> {

    /**
    * sku id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 字段值
    */
    @TableField("field_value")
    private String fieldValue;
    /**
    * 字段
    */
    @TableField("field_code")
    private String fieldCode;


    public static final String SKU_ID = "sku_id";

    public static final String FIELD_VALUE = "field_value";

    public static final String FIELD_CODE = "field_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}