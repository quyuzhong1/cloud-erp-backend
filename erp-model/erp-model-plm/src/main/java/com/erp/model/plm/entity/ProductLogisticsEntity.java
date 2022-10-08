package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品物流信息表
 * @Author Luo_WG
 * @Date 2022/9/22 16:03
 **/
@TableName(value ="product_logistics")
@Data
public class ProductLogisticsEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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
    @TableField(value = "declare_price")
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

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name")
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name")
    private String updateUserName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}