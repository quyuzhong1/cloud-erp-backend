package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
     * sku_no
     */
    @TableField(value = "sku_no")
    private String skuNo;

    /**
     * 品名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 属性
     */
    @TableField(value = "variant_property")
    private String variantProperty;

    /**
     * 计划上市时间
     */
    @TableField(value = "plan_listing_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planListingTime;

    /**
     * 首批量产入库时间
     */
    @TableField(value = "first_mass_product_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date firstMassProductDate;
    
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
     * sku图片
     */
    @TableField(value = "images_url")
    private String imagesUrl;

    /**
     * 单位名称
     */
    @TableField(value = "unit_name")
    private String unitName;

    /**
     * 产品负责人Id
     */
    @TableField(value = "charge_id")
    private String chargeId;

    /**
     * 产品负责人姓名
     */
    @TableField(value = "charge_name")
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

    /**
     *禁止修改的字段
     */
    @TableField(exist = false)
    private List<String> disableFieldList;

    /**
     * 任务状态 0待审核，1审核中，2审核通过，3审核不通过,4，待提交
     */
    @TableField("status")
    private Integer status;

    /**
     * 流程id
     */
    @TableField("process_id")
    private String processId;

    /**
     * 流程表id
     */
    @TableField("business_process_id")
    private String businessProcessId;

    /**
     * 是否变更（0否，1是）
     */
    private Integer isChange;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}