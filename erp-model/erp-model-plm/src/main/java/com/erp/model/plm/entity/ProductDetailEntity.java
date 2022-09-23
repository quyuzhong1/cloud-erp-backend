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
    private String id;

    /**
     * 产品表id
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * sku
     */
    @TableField(value = "sku")
    private String sku;

    /**
     * 产品名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 属性
     */
    @TableField(value = "property")
    private String property;

    /**
     * 计划上市时间
     */
    @TableField(value = "plan_listing_time")
    private Date planListingTime;

    /**
     * 单位表id
     */
    @TableField(value = "unit_id")
    private String unitId;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @TableField(value = "product_state")
    private Integer productState;

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
     * sku图片Id
     */
    @TableField(value = "images_url")
    private String imagesUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}