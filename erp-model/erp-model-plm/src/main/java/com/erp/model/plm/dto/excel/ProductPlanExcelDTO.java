package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.enums.ProductTypeEnum;
import com.common.business.enums.SalesPlatformEnum;
import com.common.business.enums.SeasonEnum;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.ProductStyleEnum;
import com.erp.model.plm.enums.ThreeGenerationPlanningEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:15
 */
@Data
public class ProductPlanExcelDTO implements Serializable {

    /**
     *  规划id
     */
    private String productPlanId;
    /**
     * 年份
     */
    @ExcelProperty(value = "*年份", index = 0)
    @FieldValid(fieldName = "年份", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER,maxLength = 4)
    private String  yearStr;

    /**
     * 产品经理
     */
    @ExcelProperty(value = "*产品经理", index = 1)
    @FieldValid(fieldName = "产品经理", isNotBlank = true,maxLength = 50)
    private String chargeName;

    /**
     * 所属品牌
     */
    @ExcelProperty(value = "所属品牌", index = 2)
    @FieldValid(fieldName = "所属品牌",maxLength = 200)
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
    @FieldValid(fieldName = "产品款品",enumClass = ProductStyleEnum.class)
    private String productStyleName;

    /**
     * 新品/老品
     */
    @ExcelProperty(value = "新品/老品升级", index = 6)
    @FieldValid(fieldName = "新品/老品升级",enumClass = ProductTypeEnum.class)
    private String productTypeName;

    /**
     * 三代规划（721原则）
     */
    @ExcelProperty(value = "三代规划（721原则）", index = 7)
    @FieldValid(fieldName = "三代规划（721原则）",enumClass = ThreeGenerationPlanningEnum.class)
    private String threeGenerationPlanningName;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "*产品名称（中文）", index = 8)
    @FieldValid(fieldName = "产品名称（中文）", isNotBlank = true,maxLength = 200)
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
    @FieldValid(fieldName = "sku数量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
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
    @FieldValid(fieldName = "销售平台/渠道",enumClass = SalesPlatformEnum.class)
    private String salesPlatformName;

    /**
     * 人民币定价
     */
    @ExcelProperty(value = "人民币定价", index = 21)
    @FieldValid(fieldName = "人民币定价",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String priceCnyStr;

    /**
     * 美元定价
     */
    @ExcelProperty(value = "美元定价", index = 22)
    @FieldValid(fieldName = "美元定价",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String priceUsdStr;

    /**
     * 是否需要ID设计
     */
    @ExcelProperty(value = "是否需要ID设计", index = 23)
    @FieldValid(fieldName = "是否需要ID设计",fieldValues = "是,否")
    private String isNeedIDDesignStr;

    /**
     * 是否需要结构设计
     */
    @ExcelProperty(value = "是否需要结构设计", index = 24)
    @FieldValid(fieldName = "是否需要结构设计",fieldValues = "是,否")
    private String isNeedStructuralDesignStr;

    /**
     * 目标成本
     */
    @ExcelProperty(value = "目标成本", index = 25)
    @FieldValid(fieldName = "目标成本",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String targetCostStr;

    /**
     * 摸具成本预估
     */
    @ExcelProperty(value = "摸具成本预估", index = 26)
    @FieldValid(fieldName = "摸具成本预估",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
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
    @FieldValid(fieldName = "计划调研时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planSurveyDateStr;

    /**
     * 计划立项时间
     */
    @ExcelProperty(value = "计划立项时间", index = 31)
    @FieldValid(fieldName = "计划立项时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planProjectApprovalDateStr;

    /**
     * 计划首批入库时间
     */
    @ExcelProperty(value = "计划首批入库时间", index = 32)
    @FieldValid(fieldName = "计划首批入库时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planFirstMassStockInDateStr;

    /**
     * 计划上市季节
     */
    @ExcelProperty(value = "计划上市季节", index = 33)
    @FieldValid(fieldName = "计划上市季节",enumClass = SeasonEnum.class)
    private String planMarketingSeasonName;

    /**
     * 计划上市时间
     */
    @ExcelProperty(value = "计划上市时间", index = 34)
    @FieldValid(fieldName = "计划上市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planListingDateStr;

    /**
     * 一月销售额
     */
    @ExcelProperty(value = "一月销售额", index = 35)
    @FieldValid(fieldName = "一月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String januaryAmountStr;

    /**
     * 二月销售额
     */
    @ExcelProperty(value = "二月销售额", index = 36)
    @FieldValid(fieldName = "二月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String februaryAmountStr;

    /**
     * 三月销售额
     */
    @ExcelProperty(value = "三月销售额", index = 37)
    @FieldValid(fieldName = "三月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String marchAmountStr;

    /**
     * 四月销售额
     */
    @ExcelProperty(value = "四月销售额", index = 38)
    @FieldValid(fieldName = "四月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String aprilAmountStr;

    /**
     * 五月销售额
     */
    @ExcelProperty(value = "五月销售额", index = 39)
    @FieldValid(fieldName = "五月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String mayAmountStr;

    /**
     * 六月销售额
     */
    @ExcelProperty(value = "六月销售额", index = 40)
    @FieldValid(fieldName = "六月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String juneAmountStr;

    /**
     * 七月销售额
     */
    @ExcelProperty(value = "七月销售额", index = 41)
    @FieldValid(fieldName = "七月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String julyAmountStr;

    /**
     * 八月销售额
     */
    @ExcelProperty(value = "八月销售额", index = 42)
    @FieldValid(fieldName = "八月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String augustAmountStr;

    /**
     * 九月销售额
     */
    @ExcelProperty(value = "九月销售额", index = 43)
    @FieldValid(fieldName = "九月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String septemberAmountStr;

    /**
     * 十月销售额
     */
    @ExcelProperty(value = "十月销售额", index = 44)
    @FieldValid(fieldName = "十月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String octoberAmountStr;

    /**
     * 十一月销售额
     */
    @ExcelProperty(value = "十一月销售额", index = 45)
    @FieldValid(fieldName = "十一月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String novemberAmountStr;

    /**
     * 十二月销售额
     */
    @ExcelProperty(value = "十二月销售额", index = 46)
    @FieldValid(fieldName = "十二月销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String decemberAmountStr;

    /**
     * 一月销量
     */
    @ExcelProperty(value = "一月销量", index = 47)
    @FieldValid(fieldName = "一月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String januaryQtyStr;

    /**
     * 二月销量
     */
    @ExcelProperty(value = "二月销量", index = 48)
    @FieldValid(fieldName = "二月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String februaryQtyStr;

    /**
     * 三月销量
     */
    @ExcelProperty(value = "三月销量", index = 49)
    @FieldValid(fieldName = "三月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String marchQtyStr;

    /**
     * 四月销量
     */
    @ExcelProperty(value = "四月销量", index = 50)
    @FieldValid(fieldName = "四月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String aprilQtyStr;

    /**
     * 五月销量
     */
    @ExcelProperty(value = "五月销量", index = 51)
    @FieldValid(fieldName = "五月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String mayQtyStr;

    /**
     * 六月销量
     */
    @ExcelProperty(value = "六月销量", index = 52)
    @FieldValid(fieldName = "六月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String juneQtyStr;

    /**
     * 七月销量
     */
    @ExcelProperty(value = "七月销量", index = 53)
    @FieldValid(fieldName = "七月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String julyQtyStr;

    /**
     * 八月销量
     */
    @ExcelProperty(value = "八月销量", index = 54)
    @FieldValid(fieldName = "八月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String augustQtyStr;

    /**
     * 九月销量
     */
    @ExcelProperty(value = "九月销量", index = 55)
    @FieldValid(fieldName = "九月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String septemberQtyStr;

    /**
     * 十月销量
     */
    @ExcelProperty(value = "十月销量", index = 56)
    @FieldValid(fieldName = "十月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String octoberQtyStr;

    /**
     * 十一月销量
     */
    @ExcelProperty(value = "十一月销量", index = 57)
    @FieldValid(fieldName = "十一月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String novemberQtyStr;

    /**
     * 十二月销量
     */
    @ExcelProperty(value = "十二月销量", index = 58)
    @FieldValid(fieldName = "十二月销量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String decemberQtyStr;

    /**
     * 其他备注
     */
    @ExcelProperty(value = "其他备注", index = 59)
    @FieldValid(fieldName = "其他备注",maxLength = 500)
    private String remark;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 60)
    private String errorMsg;



}
