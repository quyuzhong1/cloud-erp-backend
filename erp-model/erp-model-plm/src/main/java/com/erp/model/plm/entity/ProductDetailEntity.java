package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
* @Description 产品sku表
* @Author Luo_WG
* @Date 2022/9/22 15:18
**/
@TableName(value ="product_detail")
@Data
@NoArgsConstructor
public class ProductDetailEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键id")
    private String id;

    /**
     * 产品表id
     */
    @TableField(value = "product_id")
    @ApiModelProperty(value = "产品表id")
    private String productId;

    /**
     * sku_no
     */
    @TableField(value = "sku_no")
    @ApiModelProperty(value = "skuNo")
    private String skuNo;

    /**
     * 产品名称
     */
    @TableField(value = "name")
    @ApiModelProperty(value = "产品名称")
    private String name;

    /**
     * 属性
     */
    @TableField(value = "variant_property")
    @ApiModelProperty(value = "属性")
    private String variantProperty;

    /**
     * 计划上市时间
     */
    @TableField(value = "plan_listing_time")
    @ApiModelProperty(value = "计划上市时间")
    private Date planListingTime;

    /**
     * 单位表id
     */
    @TableField(value = "unit_id")
    @ApiModelProperty(value = "单位表id")
    private String unitId;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @TableField(value = "product_state")
    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发")
    private Integer productState;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    /**
     * sku图片
     */
    @TableField(value = "images_url")
    @ApiModelProperty(value = "sku图片")
    private String imagesUrl;

    /**
     * 单位名称
     */
    @TableField(value = "unit_name")
    @ApiModelProperty(value = "单位名称")
    private String unitName;

    /**
     * 产品负责人Id
     */
    @TableField(value = "charge_id")
    @ApiModelProperty(value = "产品负责人Id")
    private String chargeId;

    /**
     * 产品负责人姓名
     */
    @TableField(value = "charge_name")
    @ApiModelProperty(value = "产品负责人姓名")
    private String chargeName;

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