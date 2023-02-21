package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 18:34
 */
@TableName(value ="product_plan")
@Data
@NoArgsConstructor
public class ProductPlanEntity extends BaseEntity {

    /**
     * 年份
     */
    @TableField(value = "year")
    private Integer  year;

    /**
     * 产品ID
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * 产品示意图URL
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * SPU（型号Model）
     */
    @TableField(value = "spu_no")
    private String spuNo;

    /**
     * 产品名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 产品名称（英文）
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 产品状态
     */
    @TableField(value = "product_status")
    private String productStatus;

    /**
     * 产品分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 产品分类ID
     */
    @TableField(value = "category_id")
    private String categoryId;

    /**
     * 产品等级
     */
    @TableField(value = "grade")
    private String grade;

    /**
     * 产品等级ID
     */
    @TableField(value = "grade_id")
    private String gradeId;

    /**
     * 产品经理
     */
    @TableField(value = "charge_name")
    private String chargeName;

    /**
     * 产品经理ID
     */
    @TableField(value = "charge_id")
    private String chargeId;

    /**
     * 新品/老品
     */
    @TableField(value = "product_type")
    private String productType;

    /**
     * 品牌名称
     */
    @TableField(value = "brand_name")
    private String brandName;

    /**
     * 品牌ID
     */
    @TableField(value = "brand_id")
    private String brandId;

    /**
     * 对标竞品链接
     */
    @TableField(value = "competitive_product_link")
    private String competitiveProductLink;

    /**
     * 项目类型（同产品属性）
     */
    @TableField(value = "property")
    private String property;

    /**
     * 项目类型ID（同产品属性ID）
     */
    @TableField(value = "property_id")
    private String propertyId;

    /**
     * 产品款品
     */
    @TableField(value = "product_style")
    private String productStyle;

    /**
     * 三代规划（721原则）
     */
    @TableField(value = "three_generation_planning")
    private String threeGenerationPlanning;

    /**
     * 核心专利
     */
    @TableField(value = "patent")
    private String patent;

    /**
     * sku数量
     */
    @TableField(value = "sku_qty")
    private String skuQty;

    /**
     * 规格参数
     */
    @TableField(value = "specification")
    private String specification;


    /**
     * 主要卖点描述
     */
    @TableField(value = "selling_point_desc")
    private String sellingPointDesc;


    /**
     * 应用场景
     */
    @TableField(value = "application_scenario")
    private String applicationScenario;


    /**
     * 使用场景描述
     */
    @TableField(value = "application_scenario_desc")
    private String applicationScenarioDesc;

    /**
     * 场景类目
     */
    @TableField(value = "scene_category")
    private String sceneCategory;

    /**
     * 是否需要ID设计
     */
    @TableField(value = "is_need_ID_design")
    private String isNeedIDDesign;

    /**
     * 是否需要结构设计
     */
    @TableField(value = "is_need_structural_design")
    private String isNeedStructuralDesign;

    /**
     * 计划调研时间
     */
    @TableField(value = "plan_survey_date")
    private LocalDate planSurveyDate;

    /**
     * 计划立项时间
     */
    @TableField(value = "plan_project_approval_date")
    private LocalDate planProjectApprovalDate;

    /**
     * 实际立项时间
     */
    @TableField(value = "project_approval_date")
    private LocalDate projectApprovalDate;

    /**
     * 计划上市季节
     */
    @TableField(value = "plan_marketing_season")
    private String planMarketingSeason;

    /**
     * 计划首批入库时间
     */
    @TableField(value = "plan_first_mass_stock_in_date")
    private LocalDate planFirstMassStockInDate;

    /**
     * 实际首批入库时间
     */
    @TableField(value = "first_mass_stock_in_date")
    private LocalDate firstMassStockInDate;

    /**
     * 计划上市时间
     */
    @TableField(value = "plan_listing_date")
    private LocalDate planListingDate;

    /**
     * 实际上市时间
     */
    @TableField(value = "listing_date")
    private LocalDate listingDate;

}
