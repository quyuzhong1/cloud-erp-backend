package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class ProductInfoEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品名
     */
    @TableField("name")
    private String name;

    /**
     * 产品名称（英文）
     */
    @TableField("name_en")
    private String nameEn;

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
    @TableField(value = "approval_time")
    private LocalDateTime approvalTime;

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
     * 是否是产品开发管理的数据：1 是
     */
    @TableField(value = "is_finished_product_dev")
    private Integer isFinishedProductDev;

    /**
     * 委托开发成本
     */
    @TableField(value = "entrusted_develop_cost")
    private BigDecimal entrustedDevelopCost;

    /**
     * 模具成本
     */
    @TableField(value = "mold_cost")
    private BigDecimal moldCost;

    /**
     * 样品费用
     */
    @TableField(value = "sample_fee")
    private BigDecimal sampleFee;

    /**
     * 是否客户定制(0否，1是)
     */
    @TableField(value = "is_customized")
    private Integer isCustomized;

    /**
     * 销售渠道
     */
    @TableField(value = "sales_channel")
    private String salesChannel;

    /**
     * 示意图url
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * normal 正常  postpone 延期   risk 风险  no 暂无
     * 产品进展
     */
    @TableField(value = "progress_status")
    private String progressStatus;

    /**
     * 项目经理
     */
    @TableField(value = "project_charge_id")
    private String projectChargeId;

    /**
     * 模板id
     */
    @TableField(value = "template_id")
    private String templateId;

    /**
     * 产品版本号
     */
    @TableField(value = "product_version")
    private Integer productVersion;


    /**
     * 暂停前的状态
     */
    @TableField(value = "suspend_before_status")
    private Integer suspendBeforeStatus;

}
