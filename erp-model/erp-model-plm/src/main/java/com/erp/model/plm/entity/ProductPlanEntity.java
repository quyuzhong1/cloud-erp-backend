package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

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
    private Integer  year;
    /**
     * 产品ID
     */
    private String productId;

    /**
     * 产品示意图URL
     */
    private String imageUrl;

    /**
     * SPU（型号Model）
     */
    private String spuNo;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品名称（英文）
     */
    private String nameEn;

    /**
     * 产品状态
     */
    private String productStatus;

    /**
     * 产品分类
     */
    private String category;

    /**
     * 产品分类ID
     */
    private String categoryId;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品等级ID
     */
    private String gradeId;

    /**
     * 产品经理
     */
    private String chargeName;

    /**
     * 产品经理ID
     */
    private String chargeId;

    /**
     * 新品/老品
     */
    private String productType;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 品牌ID
     */
    private String brandId;

    /**
     * 对标竞品链接
     */
    private String competitiveProductLink;

    /**
     * 项目类型（同产品属性）
     */
    private String property;

    /**
     * 项目类型ID（同产品属性ID）
     */
    private String propertyId;

    /**
     * 产品款品
     */
    private String productStyle;

    /**
     * 三代规划（721原则）
     */
    private String threeGenerationPlanning;

    /**
     * 核心专利
     */
    private String patent;

    /**
     * sku数量
     */
    private String skuQty;

    /**
     * 规格参数
     */
    private String specification;


    /**
     * 主要卖点描述
     */
    private String sellingPointDesc;


    /**
     * 应用场景
     */
    private String applicationScenario;


    /**
     * 使用场景描述
     */
    private String applicationScenarioDesc;

    /**
     * 场景类目
     */
    private String sceneCategory;

    /**
     * 是否需要ID设计
     */
    private String isNeedIDDesign;

    /**
     * 是否需要结构设计
     */
    private String isNeedStructuralDesign;

    /**
     * 计划调研时间
     */
    private LocalDate planSurveyDate;

    /**
     * 计划立项时间
     */
    private LocalDate planProjectApprovalDate;

    /**
     * 实际立项时间
     */
    private LocalDate projectApprovalDate;

    /**
     * 计划上市季节
     */
    private String planMarketingSeason;

    /**
     * 计划首批入库时间
     */
    private LocalDate planFirstMassStockInDate;

    /**
     * 实际首批入库时间
     */
    private LocalDate firstMassStockInDate;

    /**
     * 计划上市时间
     */
    private LocalDate planListingDate;

    /**
     * 实际上市时间
     */
    private LocalDate listingDate;

}
