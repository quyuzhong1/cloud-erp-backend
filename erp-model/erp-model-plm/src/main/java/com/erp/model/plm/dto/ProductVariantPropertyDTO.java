package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品变体属性值表
 * @TableName product_variant_property
 */
@Data
@NoArgsConstructor
public class ProductVariantPropertyDTO implements Serializable {

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "属性值")
    private String propertyValue;

    @ApiModelProperty(value = "变体类型表id")
    private String variantId;

    private static final long serialVersionUID = 1L;
}