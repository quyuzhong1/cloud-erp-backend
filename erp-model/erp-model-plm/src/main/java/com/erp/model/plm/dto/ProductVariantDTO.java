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
 * 产品变体类型属性表
 * @TableName product_variant
 */
@Data
@NoArgsConstructor
public class ProductVariantDTO implements Serializable {

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "变体属性类型")
    private String propertyType;

    @ApiModelProperty(value = "产品表id")
    private String productId;

    private static final long serialVersionUID = 1L;
}