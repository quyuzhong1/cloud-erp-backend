package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description 产品证书表
 * @Author Luo_WG
 * @Date 2022/9/23 15:22
 **/
@TableName(value ="product_certificate")
@Data
public class ProductCertificateEntity extends BaseEntity implements Serializable {

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 证书图片
     */
    @TableField(value = "certificate_img")
    private String certificateImg;

    /**
     * 证书有效期
     */
    @TableField(value = "certificate_valid_time")
    private LocalDateTime certificateValidTime;

    /**
     * 产品表id
     */
    @TableField(value = "product_id")
    private String productId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}