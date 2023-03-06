package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 模板属性关系表
 * </p>
 *
 * @author admin
 * @since 2023-03-06
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("template_ref_property")
public class TemplateRefPropertyEntity extends BaseEntity<TemplateRefPropertyEntity> {

    /**
     * 模板id
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 产品属性id 对应basic_dict 表 type=productProperty
     */
    @TableField("product_property_id")
    private String productPropertyId;


    public static final String TEMPLATE_ID = "template_id";

    public static final String PRODUCT_PROPERTY_ID = "product_property_id";


}
