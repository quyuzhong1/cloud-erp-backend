package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description 产品物流信息表
 * @Author Luo_WG
 * @Date 2022/9/22 16:03
 **/
@TableName(value ="product_logistics")
@Data
public class ProductLogisticsEntity extends BaseEntity implements Serializable {

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 产品属性
     */
    @TableField(value = "product_property")
    private String productProperty;

    /**
     * 产品属性id
     */
    @TableField(value = "product_property_id")
    private String productPropertyId;

    /**
     * 报关中文名
     */
    @TableField(value = "declare_chinese_name")
    private String declareChineseName;

    /**
     * 报关英文名
     */
    @TableField(value = "declare_english_name")
    private String declareEnglishName;

    /**
     * 报关申报价格
     */
    @TableField(value = "declare_price", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal declarePrice;

    /**
     * 海关编码
     */
    @TableField(value = "customs_code")
    private String customsCode;

    /**
     * 申报要素
     */
    @TableField(value = "declare_element")
    private String declareElement;

    /**
     * 英文材质
     */
    @TableField(value = "english_material")
    private String englishMaterial;

    /**
     * 英文用途
     */
    @TableField(value = "english_usage")
    private String englishUsage;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}