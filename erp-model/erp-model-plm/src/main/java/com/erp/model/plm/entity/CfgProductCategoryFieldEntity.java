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
 * 产品分类字段配置表
 * </p>
 *
 * @author Lambda
 * @since 2023-11-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_product_category_field")
public class CfgProductCategoryFieldEntity extends BaseEntity<CfgProductCategoryFieldEntity> {

    /**
    * 分类id
    */
    @TableField("category_id")
    private String categoryId;

    @TableField("category_name")
    private String categoryName;
    /**
    * 字段
    */
    @TableField("field_code")
    private String fieldCode;
    /**
    * 字段名
    */
    @TableField("field_name")
    private String fieldName;


    public static final String CATEGORY_ID = "category_id";

    public static final String FIELD_CODE = "field_code";

    public static final String FIELD_NAME = "field_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}