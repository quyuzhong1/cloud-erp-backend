package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 产品信息表
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("product_info")
public class BiProductInfoEntity extends BaseEntity<BiProductInfoEntity> {

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
     * 产品品牌
     */
    @TableField("brand_name")
    private String brandName;

    /**
     * 立项状态 0 待规划 1 调研中  2：ID设计中  3::已立项  4：已终止
     */
    @TableField("approval_status")
    private Integer approvalStatus;

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

    /**
     * 产品类型 1 新产品 2 迭代产品
     */
    @TableField("type")
    private Integer type;

    /**
     * 删除标示 0 未删 1 已删
     */
    @TableField("delete_state")
    private Integer deleteState;

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
     * 等级id
     */
    @TableField("grade_id")
    private String gradeId;

    /**
     * 是否是产品开发管理的数据：1 是  
     */
    @TableField("is_finished_product_dev")
    private Integer isFinishedProductDev;

    /**
     * 立项时间
     */
    @TableField("approval_time")
    private Date approvalTime;

    /**
     * 产品版本号
     */
    @TableField("product_version")
    private Integer productVersion;

    /**
     * 委托开发成本
     */
    @TableField("entrusted_develop_cost")
    private BigDecimal entrustedDevelopCost;

    /**
     * 模具成本
     */
    @TableField("mold_cost")
    private BigDecimal moldCost;

    /**
     * 样品费用
     */
    @TableField("sample_fee")
    private BigDecimal sampleFee;

    /**
     * 是否客户定制(0否，1是)
     */
    @TableField("is_customized")
    private Integer isCustomized;

    /**
     * 销售渠道
     */
    @TableField("sales_channel")
    private String salesChannel;

    /**
     * 产品名称（英文）
     */
    @TableField("name_en")
    private String nameEn;

    /**
     * 产品示意图url
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * normal 正常  postpone 延期   risk 风险  no 暂无
     */
    @TableField("progress_status")
    private String progressStatus;

    /**
     * 项目经理id
     */
    @TableField("project_charge_id")
    private String projectChargeId;

    /**
     * 模板id
     */
    @TableField("template_id")
    private String templateId;


    public static final String NAME = "name";

    public static final String CATEGORY = "category";

    public static final String PROPERTY = "property";

    public static final String CHARGE_NAME = "charge_name";

    public static final String CHARGE_ID = "charge_id";

    public static final String GRADE = "grade";

    public static final String BRAND_NAME = "brand_name";

    public static final String APPROVAL_STATUS = "approval_status";

    public static final String SYS_FIELD = "sys_field";

    public static final String PROPERTY_ID = "property_id";

    public static final String BRAND_ID = "brand_id";

    public static final String CATEGORY_ID = "category_id";

    public static final String TYPE = "type";

    public static final String DELETE_STATE = "delete_state";

    public static final String SPU_NO = "spu_no";

    public static final String SELL_SPOT = "sell_spot";

    public static final String FUNCTION_DESC = "function_desc";

    public static final String USAGE_DESC = "usage_desc";

    public static final String PIRATE_RISK = "pirate_risk";

    public static final String MATERIALS = "materials";

    public static final String SPEC_TYPE = "spec_type";

    public static final String SALE_METHOD = "sale_method";

    public static final String RELEVANCE_PRODUCT_ID = "relevance_product_id";

    public static final String GRADE_ID = "grade_id";

    public static final String IS_FINISHED_PRODUCT_DEV = "is_finished_product_dev";

    public static final String APPROVAL_TIME = "approval_time";

    public static final String PRODUCT_VERSION = "product_version";

    public static final String ENTRUSTED_DEVELOP_COST = "entrusted_develop_cost";

    public static final String MOLD_COST = "mold_cost";

    public static final String SAMPLE_FEE = "sample_fee";

    public static final String IS_CUSTOMIZED = "is_customized";

    public static final String SALES_CHANNEL = "sales_channel";

    public static final String NAME_EN = "name_en";

    public static final String IMAGE_URL = "image_url";

    public static final String PROGRESS_STATUS = "progress_status";

    public static final String PROJECT_CHARGE_ID = "project_charge_id";

    public static final String TEMPLATE_ID = "template_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
