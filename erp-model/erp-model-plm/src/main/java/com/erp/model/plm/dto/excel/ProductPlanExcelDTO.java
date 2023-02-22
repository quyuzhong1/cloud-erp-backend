package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.common.business.enums.ProductTypeEnum;
import com.erp.common.business.enums.SalesPlatformEnum;
import com.erp.common.business.enums.SeasonEnum;
import com.erp.model.plm.enums.ProductStyleEnum;
import com.erp.model.plm.enums.ThreeGenerationPlanningEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:15
 */
@Data
@NoArgsConstructor
public class ProductPlanExcelDTO implements Serializable {

    /**
     * 年份
     */
    @ExcelProperty(value = "年份", index = 0)
    @FieldValid(isNotNull = true,formatPattern = FieldFormatPatternTypeEnum.integer,maxLength = 4)
    private String  year;

    /**
     * 产品经理
     */
    @ExcelProperty(value = "产品经理", index = 1)
    @FieldValid(isNotNull = true)
    private String chargeName;

    /**
     * 所属品牌
     */
    @ExcelProperty(value = "所属品牌", index = 2)
    private String brandName;


    /**
     * 产品分类
     */
    @ExcelProperty(value = "产品分类", index = 3)
    private String category;

    /**
     * 产品等级
     */
    @ExcelProperty(value = "产品等级", index = 4)
    private String grade;

    /**
     * 产品款品
     */
    @ExcelProperty(value = "产品款品", index = 5)
    @FieldValid(enumClass = ProductStyleEnum.class)
    private String productStyleName;

    /**
     * 新品/老品
     */
    @ExcelProperty(value = "新品/老品", index = 6)
    @FieldValid(enumClass = ProductTypeEnum.class)
    private String productTypeName;

    /**
     * 三代规划（721原则）
     */
    @ExcelProperty(value = "三代规划（721原则）", index = 7)
    @FieldValid(enumClass = ThreeGenerationPlanningEnum.class)
    private String threeGenerationPlanningName;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 8)
    @FieldValid(isNotNull = true,maxLength = 200)
    private String name;

    /**
     * 产品名称（英文）
     */
    @ExcelProperty(value = "产品名称（英文）", index = 9)
    private String nameEn;

    /**
     * 核心专利
     */
    @ExcelProperty(value = "核心专利", index = 10)
    private String patent;

    /**
     * 对标竞品链接
     */
    @ExcelProperty(value = "对标竞品链接", index = 11)
    private String competitiveProductLink;

    /**
     * SPU（型号Model）
     */
    @ExcelProperty(value = "SPU（型号Model）", index = 12)
    private String spuNo;

    /**
     * sku数量
     */
    @ExcelProperty(value = "sku数量", index = 13)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String skuQtyStr;

    /**
     * 规格参数
     */
    @ExcelProperty(value = "规格参数", index = 14)
    private String specification;

    /**
     * 应用场景
     */
    @ExcelProperty(value = "应用场景", index = 15)
    private String applicationScenario;

    /**
     * 场景类目
     */
    @ExcelProperty(value = "场景类目", index = 16)
    private String sceneCategory;

    /**
     * 使用场景描述
     */
    @ExcelProperty(value = "使用场景描述", index = 17)
    private String applicationScenarioDesc;

    /**
     * 主要卖点描述
     */
    @ExcelProperty(value = "主要卖点描述", index = 18)
    private String sellingPointDesc;


    /**
     * 销售目标国家
     */
    @ExcelProperty(value = "销售目标国家", index = 19)
    private String salesTargetCountry;


    /**
     * 销售平台/渠道
     */
    @ExcelProperty(value = "销售平台/渠道", index = 20)
    @FieldValid(enumClass = SalesPlatformEnum.class)
    private String salesPlatformName;

    /**
     * 人民币定价
     */
    @ExcelProperty(value = "人民币定价", index = 21)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String priceCnyStr;

    /**
     * 美元定价
     */
    @ExcelProperty(value = "美元定价", index = 22)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String priceUsdStr;

    /**
     * 是否需要ID设计
     */
    @ExcelProperty(value = "是否需要ID设计", index = 23)
    @FieldValid(fieldValues = "是,否")
    private String isNeedIDDesignStr;

    /**
     * 是否需要结构设计
     */
    @ExcelProperty(value = "是否需要结构设计", index = 24)
    @FieldValid(fieldValues = "是,否")
    private String isNeedStructuralDesignStr;

    /**
     * 目标成本
     */
    @ExcelProperty(value = "目标成本", index = 25)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String targetCostStr;

    /**
     * 摸具成本预估
     */
    @ExcelProperty(value = "摸具成本预估", index = 26)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String estimatedMoldCostStr;

    /**
     * 供应商状态
     */
    @ExcelProperty(value = "供应商状态", index = 27)
    private String supplierStatus;

    /**
     * 主要供应商名称
     */
    @ExcelProperty(value = "主要供应商名称", index = 28)
    private String mainSupplierName;

    /**
     * 项目类型（同产品属性）
     */
    @ExcelProperty(value = "项目类型", index = 29)
    private String property;

    /**
     * 计划调研时间
     */
    @ExcelProperty(value = "计划调研时间", index = 30)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.date)
    private String planSurveyDateStr;

    /**
     * 计划立项时间
     */
    @ExcelProperty(value = "计划立项时间", index = 31)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.date)
    private String planProjectApprovalDateStr;

    /**
     * 计划首批入库时间
     */
    @ExcelProperty(value = "计划首批入库时间", index = 32)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.date)
    private String planFirstMassStockInDateStr;

    /**
     * 计划上市季节
     */
    @ExcelProperty(value = "计划上市季节", index = 33)
    @FieldValid(enumClass = SeasonEnum.class)
    private String planMarketingSeasonName;

    /**
     * 计划上市时间
     */
    @ExcelProperty(value = "计划上市时间", index = 34)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.date)
    private String planListingDateStr;

    /**
     * 一月销售额
     */
    @ExcelProperty(value = "一月销售额", index = 35)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String januaryAmountStr;

    /**
     * 二月销售额
     */
    @ExcelProperty(value = "二月销售额", index = 36)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String februaryAmountStr;

    /**
     * 三月销售额
     */
    @ExcelProperty(value = "三月销售额", index = 37)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String marchAmountStr;

    /**
     * 四月销售额
     */
    @ExcelProperty(value = "四月销售额", index = 38)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String aprilAmountStr;

    /**
     * 五月销售额
     */
    @ExcelProperty(value = "五月销售额", index = 39)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String mayAmountStr;

    /**
     * 六月销售额
     */
    @ExcelProperty(value = "六月销售额", index = 40)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String juneAmountStr;

    /**
     * 七月销售额
     */
    @ExcelProperty(value = "七月销售额", index = 41)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String julyAmountStr;

    /**
     * 八月销售额
     */
    @ExcelProperty(value = "八月销售额", index = 42)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String augustAmountStr;

    /**
     * 九月销售额
     */
    @ExcelProperty(value = "九月销售额", index = 43)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String septemberAmountStr;

    /**
     * 十月销售额
     */
    @ExcelProperty(value = "十月销售额", index = 44)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String octoberAmountStr;

    /**
     * 十一月销售额
     */
    @ExcelProperty(value = "十一月销售额", index = 45)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String novemberAmountStr;

    /**
     * 十二月销售额
     */
    @ExcelProperty(value = "十二月销售额", index = 46)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.amount)
    private String decemberAmountStr;

    /**
     * 一月销量
     */
    @ExcelProperty(value = "一月销量", index = 47)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String januaryQtyStr;

    /**
     * 二月销量
     */
    @ExcelProperty(value = "二月销量", index = 48)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String februaryQtyStr;

    /**
     * 三月销量
     */
    @ExcelProperty(value = "三月销量", index = 49)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String marchQtyStr;

    /**
     * 四月销量
     */
    @ExcelProperty(value = "四月销量", index = 50)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String aprilQtyStr;

    /**
     * 五月销量
     */
    @ExcelProperty(value = "五月销量", index = 51)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String mayQtyStr;

    /**
     * 六月销量
     */
    @ExcelProperty(value = "六月销量", index = 52)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String juneQtyStr;

    /**
     * 七月销量
     */
    @ExcelProperty(value = "七月销量", index = 53)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String julyQtyStr;

    /**
     * 八月销量
     */
    @ExcelProperty(value = "八月销量", index = 54)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String augustQtyStr;

    /**
     * 九月销量
     */
    @ExcelProperty(value = "九月销量", index = 55)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String septemberQtyStr;

    /**
     * 十月销量
     */
    @ExcelProperty(value = "十月销量", index = 56)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String octoberQtyStr;

    /**
     * 十一月销量
     */
    @ExcelProperty(value = "十一月销量", index = 57)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String novemberQtyStr;

    /**
     * 十二月销量
     */
    @ExcelProperty(value = "十二月销量", index = 58)
    @FieldValid(formatPattern = FieldFormatPatternTypeEnum.positiveInteger)
    private String decemberQtyStr;

    /**
     * 其他备注
     */
    @ExcelProperty(value = "其他备注", index = 59)
    private String remark;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 60)
    private String errorMsg;



}
