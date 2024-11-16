package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName product_images
 */
@TableName(value ="product_images")
@Data
public class ProductImagesEntity extends BaseEntity<ProductImagesEntity> implements Serializable {

    /**
     * 产品表id
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * 产品sku明细表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 图片地址
     */
    @TableField(value = "images_url")
    private String imagesUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}