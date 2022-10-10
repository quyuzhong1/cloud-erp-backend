package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 产品变体类型属性表
 * @TableName product_variant
 */
@Data
@NoArgsConstructor
public class ProductVariantDTO implements Serializable {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private String id;

    /**
     * 变体属性类型
     */
    @ApiModelProperty(value = "变体属性类型")
    private String propertyType;

    /**
     * 产品表id
     */
    @ApiModelProperty(value = "产品表id")
    private String productId;

    /**
     * 变体值
     */
    @ApiModelProperty(value = "变体值")
    private List<ProductVariantPropertyDTO> productVariantPropertyList;


    private static final long serialVersionUID = 1L;
}