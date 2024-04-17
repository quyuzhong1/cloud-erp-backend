package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

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
     * 产品尺寸长
     */
    @TableField(value = "product_length")
    private BigDecimal productLength;
    /**
     * 产品尺寸宽
     */
    @TableField(value = "product_width")
    private BigDecimal productWidth;
    /**
     * 产品尺寸高
     */
    @TableField(value = "product_height")
    private BigDecimal productHeight;

    @TableField(value = "product_size")
    @Deprecated
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
     * 箱规长
     */
    @TableField(value = "box_length")
    private BigDecimal boxLength;
    /**
     * 箱规宽
     */
    @TableField(value = "box_width")
    private BigDecimal boxWidth;
    /**
     * 箱规高
     */
    @TableField(value = "box_height")
    private BigDecimal boxHeight;

    /**
     * @deprecated (初始化数据后删除)
     */
    @TableField(value = "box_size")
    @Deprecated
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