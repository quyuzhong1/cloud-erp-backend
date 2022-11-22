package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 产品信息表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_info")
public class ProductInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 产品名
     */
    @TableField("name")
    private String name;

    /**
     * 产品类别
     */
    @TableField("category")
    private String category;

    /**
     * 产品属性
     */
    @TableField("property")
    private String property;

    /**
     * 产品负责人
     */
    @TableField("charge_name")
    private String chargeName;

    /**
     * 负责人id
     */
    @TableField("charge_id")
    private String chargeId;

    /**
     * 产品等级
     */
    @TableField("grade")
    private String grade;

    /**
     * 产品等级id
     */
    @TableField("grade_id")
    private String gradeId;

    /**
     * 产品品牌
     */
    @TableField("brand_name")
    private String brandName;



    /**
     * 立项状态 0 待规划 1 调研中  3：ID设计中  4::已立项  5：已终止
     */
    @TableField("approval_status")
    private Integer approvalStatus;

    /**
     * 立项日期
     */
    @TableField(value = "approval_time",insertStrategy =FieldStrategy.IGNORED ,updateStrategy = FieldStrategy.IGNORED)
    private Date approvalTime;

    /**
     * 以JSON 字符串保存
     */
    @TableField("sys_field")
    private String sysField;


    /**
     * 产品属性id
     */
    @TableField("property_id")
    private String propertyId;


    /**
     * 品牌id
     */
    @TableField("brand_id")
    private String brandId;

    /**
     * 分类id
     */
    @TableField("category_id")
    private String categoryId;

    @TableField("delete_state")
    private Integer deleteState;

    @TableField("type")
    private Integer type;



    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * spu
     */
    @TableField("spu_no")
    private String spuNo;

    /**
     * 产品卖点
     */
    @TableField("sell_spot")
    private String sellSpot;

    /**
     * 产品功能描述
     */
    @TableField("function_desc")
    private String functionDesc;

    /**
     * 产品用途
     */
    @TableField("usage_desc")
    private String usageDesc;

    /**
     * 存在侵权风险 1：有侵权风险 2：无侵权风险
     */
    @TableField("pirate_risk")
    private Integer pirateRisk;

    /**
     * 主要材质
     */
    @TableField("materials")
    private String materials;

    /**
     * 规格类型  1：无规格  2：多规格
     */
    @TableField("spec_type")
    private Integer specType;

    /**
     * 销售方式
     */
    @TableField("sale_method")
    private String saleMethod;


    /**
     * 关联产品id
     */
    @TableField("relevance_product_id")
    private String relevanceProductId;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name")
    private String createUserName;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name")
    private String updateUserName;

    /**
     * 是否是产品开发管理的数据：1 是
     */
    @TableField(value = "is_finished_product_dev")
    private Integer isFinishedProductDev;

    /**
     * 版本
     */
    @TableField(value = "version")
    private Integer version;


}
