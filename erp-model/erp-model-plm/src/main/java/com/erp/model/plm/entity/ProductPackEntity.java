package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description 产品包装信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:05
 **/
@TableName(value ="product_pack")
@Data
public class ProductPackEntity extends BaseEntity implements Serializable {

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 产品尺寸
     */
    @TableField(value = "product_size")
    private String productSize;

    /**
     * 毛重
     */
    @TableField(value = "gross_weight", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal grossWeight;

    /**
     * 净重
     */
    @TableField(value = "net_weight", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal netWeight;

    /**
     * 箱规
     */
    @TableField(value = "box_size")
    private String boxSize;

    /**
     * 单箱重量
     */
    @TableField(value = "box_weight", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal boxWeight;

    /**
     * 单箱数量
     */
    @TableField(value = "box_qty", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal boxQty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}