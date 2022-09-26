package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName product_images
 */
@TableName(value ="product_images")
@Data
public class ProductImagesEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}