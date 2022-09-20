package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
    @TableField("person_in_charge")
    private String personInCharge;

    /**
     * 负责人id
     */
    @TableField("in_charge_id")
    private String inChargeId;

    /**
     * 产品等级
     */
    @TableField("grade")
    private String grade;

    /**
     * 产品品牌
     */
    @TableField("brand")
    private String brand;

    /**
     * 项目状态 0 未启动 1 ;已启动 2 进行中 3 已完成  4 已终止
     */
    @TableField("project_status")
    private Integer itemStatus;

    /**
     * 立项状态 0 待规划 1 调研中  3：ID设计中  4::已立项  5：已终止
     */
    @TableField("approval_status")
    private Integer approvalStatus;

    /**
     * 以JSON 字符串保存
     */
    @TableField("sys_field")
    private String sysField;

    /**
     * 产品图片地址
     */
    @TableField("images_url")
    private String imagesUrl;

    /**
     * 产品属性id
     */
    @TableField("property_id")
    private String propertyId;

    /**
     * 关联产品id
     */
    @TableField("ref_product_id")
    private String refProductId;

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


}
