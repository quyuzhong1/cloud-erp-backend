package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 产品选择的变体属性表
 * @TableName product_variant_option
 */
@TableName(value ="product_variant_option")
@Data
public class ProductVariantOptionEntity implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 产品id
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * 变体类型
     */
    @TableField(value = "variant_type")
    private String variantType;

    /**
     * 变体值
     */
    @TableField(value = "variant_value")
    private String variantValue;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}