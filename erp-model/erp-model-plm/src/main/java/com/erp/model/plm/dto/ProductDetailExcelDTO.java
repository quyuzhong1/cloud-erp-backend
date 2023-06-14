package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.ProductDetailStateEnum;
import com.erp.model.plm.enums.PurchaseStateEnum;
import com.erp.model.plm.enums.SaleMethodEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 产品sku信息导入
 * @Author Luo_WG
 * @Date 2022/9/27 16:23
 **/
@Data
@NoArgsConstructor
public class ProductDetailExcelDTO {

    /**
     * spuNo
     */
    //@ExcelProperty( value = "spuNo", index = 0)
    @FieldValid(fieldName = "spu", isNotBlank = true,maxLength = 30,formatPattern = FieldFormatPatternTypeEnum.NOT_CHINESE)
    private String spuNo;

    /**
     * skuNo
     */
    //@ExcelProperty( value = "skuNo", index = 0)
    @FieldValid(fieldName = "sku", isNotBlank = true,maxLength = 30,formatPattern = FieldFormatPatternTypeEnum.NOT_CHINESE)
    private String skuNo;

    /**
     * 产品分类
     */
    //@ExcelProperty( value = "产品分类", index = 1)
    @FieldValid(fieldName = "产品分类", isNotBlank = true)
    private String category;

    /**
     * 销售方式
     */
    //@ExcelProperty( value = "销售方式", index = 2)
    @FieldValid(fieldName = "销售方式",enumClass = SaleMethodEnum.class)
    private String saleMethod;

    /**
     * 产品卖点
     */
    //@ExcelProperty( value = "产品卖点", index = 3)
    private String sellSpot;

    /**
     * 产品功能描述
     */
    //@ExcelProperty( value = "产品功能描述", index = 4)
    private String functionDesc;

    /**
     * 产品用途
     */
    //@ExcelProperty( value = "产品用途", index = 5)
    private String usageDesc;

    /**
     * 存在侵权风险
     */
    //@ExcelProperty( value = "存在侵权风险", index = 6)
    @FieldValid(fieldName = "存在侵权风险",fieldValues = "是,否")
    private String pirateRisk;

    /**
     * 主要材质
     */
    //@ExcelProperty(value = "主要材质", index = 7)
    private String materials;

    /**
     * 品名
     */
    //@ExcelProperty(value = "品名", index = 8)
    @FieldValid(fieldName = "品名", isNotBlank = true,maxLength = 255)
    private String name;

    /**
     * 品名（英文）
     */
    //@ExcelProperty(value = "品名（英文）", index = 9)
    @FieldValid(fieldName = "品名（英文）", isNotBlank = true,maxLength = 255)
    private String nameEn;

    /**
     * 品牌
     */
    //@ExcelProperty(value = "品牌", index = 10)
    @FieldValid(fieldName = "品牌", isNotBlank = true,maxLength = 30)
    private String brandName;

    /**
     * 产品属性
     */
    //@ExcelProperty(value = "产品属性", index = 11)
    private String property;

    /**
     * 计划上市时间
     */
    //@ExcelProperty(value = "计划上市时间", index = 12)
    @FieldValid(fieldName = "计划上市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planListingTimeStr;

    /**
     * 单位
     */
    //@ExcelProperty(value = "单位", index = 13)
    private String unitName;

    /**
     * 产品经理
     */
    //@ExcelProperty(value = "产品经理", index = 14)
    @FieldValid(fieldName = "产品经理", isNotBlank = true,maxLength = 20)
    private String chargeName;

    /**
     * 目标含税成本
     */
    //@ExcelProperty(value = "目标含税成本", index = 15)
    private String targetTaxCost;

    /**
     * 目标不含税成本
     */
    //@ExcelProperty(value = "目标不含税成本", index = 16)
    private String targetNoTaxCost;

    /**
     * 实际含税成本
     */
    //@ExcelProperty(value = "实际含税成本", index = 17)
    private String actualTaxCost;

    /**
     * 实际不含税成本
     */
    //@ExcelProperty(value = "实际不含税成本", index = 18)
    private String actualNoTaxCost;

    /**
     * 标准零售价
     */
    //@ExcelProperty(value = "标准零售价", index = 19)
    private String retailPrice;

    /**
     * 目标毛利率
     */
    //@ExcelProperty(value = "目标毛利率", index = 20)
    private String targetGpm;

    /**
     * 实际毛利率(人民币)
     */
    //@ExcelProperty(value = "实际毛利率(人民币)", index = 21)
    private String actualGpmCny;

    /**
     * 实际毛利率(美元)
     */
    //@ExcelProperty(value = "实际毛利率(美元)", index = 22)
    private String actualGpmUsd;

    /**
     * 年目标销售量
     */
    //@ExcelProperty(value = "年目标销售量", index = 23)
    @FieldValid(fieldName = "年目标销售量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String yearSaleQtyStr;

    /**
     * 年目标销售额
     */
    //@ExcelProperty(value = "年目标销售额", index = 24)
    @FieldValid(fieldName = "年目标销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String yearSaleAmountStr;

    /**
     * 月目标销售量
     */
    //@ExcelProperty(value = "月目标销售量", index = 25)
    @FieldValid(fieldName = "月目标销售量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String monthSaleQtyStr;

    /**
     * 月目标销售额
     */
    //@ExcelProperty(value = "月目标销售额", index = 26)
    @FieldValid(fieldName = "月目标销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String monthSaleAmountStr;

    /**
     * 销售国家
     */
    //@ExcelProperty(value = "销售国家", index = 27)
    private String saleCountry;

    /**
     * 上市时间
     */
    //@ExcelProperty(value = "上市时间", index = 28)
    @FieldValid(fieldName = "上市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String listingTimeStr;

    /**
     * 退市时间
     */
    //@ExcelProperty(value = "退市时间", index = 29)
    @FieldValid(fieldName = "退市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String delistingTimeStr;

    /**
     * 图片是否完成
     */
    //@ExcelProperty(value = "图片是否完成", index = 30)
    @FieldValid(fieldName = "图片是否完成",fieldValues = "是,否")
    private String isFinishedImg;

    /**
     * 视频是否完成
     */
    //@ExcelProperty(value = "视频是否完成", index = 31)
    @FieldValid(fieldName = "视频是否完成",fieldValues = "是,否")
    private String isFinishedVideo;

    /**
     * 销售状态
     */
    //@ExcelProperty(value = "销售状态", index = 32)
    @FieldValid(fieldName = "销售状态",enumClass = SaleStateEnum.class)
    private String saleState;

    /**
     * 报关产品属性
     */
    //@ExcelProperty(value = "报关产品属性", index = 33)
    private String productProperty;

    /**
     * 报关中文名
     */
    //@ExcelProperty(value = "报关中文名", index = 34)
    private String declareChineseName;

    /**
     * 报关英文名
     */
    //@ExcelProperty(value = "报关英文名", index = 35)
    private String declareEnglishName;

    /**
     * 报关申报价格
     */
    //@ExcelProperty(value = "报关申报价格", index = 36)
    @FieldValid(fieldName = "报关申报价格",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String declarePriceStr;

    /**
     * 海关编码
     */
    //@ExcelProperty(value = "海关编码", index = 37)
    private String customsCode;

    /**
     * 申报要素
     */
    //@ExcelProperty(value = "申报要素", index = 38)
    private String declareElement;

    /**
     * 英文材质
     */
    //@ExcelProperty(value = "英文材质", index = 39)
    private String englishMaterial;

    /**
     * 英文用途
     */
    //@ExcelProperty(value = "英文用途", index = 40)
    private String englishUsage;

    /**
     * 产品尺寸(长)
     */
    //@ExcelProperty(value = "产品尺寸(长)", index = 41)
    @FieldValid(fieldName = "产品尺寸(长)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String productSizeLengthStr;

    /**
     * 产品尺寸(宽)
     */
    //@ExcelProperty(value = "产品尺寸(宽)", index = 42)
    @FieldValid(fieldName = "产品尺寸(宽)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String productSizeWideStr;

    /**
     * 产品尺寸(高)
     */
    //@ExcelProperty(value = "产品尺寸(高)", index = 43)
    @FieldValid(fieldName = "产品尺寸(高)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String productSizeHighStr;

    /**
     * 毛重
     */
    //@ExcelProperty(value = "毛重", index = 44)
    @FieldValid(fieldName = "毛重",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String grossWeightStr;

    /**
     * 净重
     */
    //@ExcelProperty(value = "净重", index = 45)
    @FieldValid(fieldName = "净重",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String netWeightStr;

    /**
     * 箱规(长)
     */
    //@ExcelProperty(value = "箱规(长)", index = 46)
    @FieldValid(fieldName = "箱规(长)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxSizeLengthStr;

    /**
     * 箱规(宽)
     */
    //@ExcelProperty(value = "箱规(宽)", index = 47)
    @FieldValid(fieldName = "箱规(宽)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxSizeWideStr;

    /**
     * 箱规(高)
     */
    //@ExcelProperty(value = "箱规(高)", index = 48)
    @FieldValid(fieldName = "箱规(高)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxSizeHighStr;

    /**
     * 单箱重量
     */
    //@ExcelProperty(value = "单箱重量", index = 49)
    @FieldValid(fieldName = "单箱重量",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxWeightStr;

    /**
     * 单箱数量
     */
    //@ExcelProperty(value = "单箱数量", index = 50)
    @FieldValid(fieldName = "单箱数量",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxQtyStr;

    /**
     * ean码
     */
    //@ExcelProperty(value = "ean码", index = 51)
    private String ean;

    /**
     * 计划首批下单量
     */
    //@ExcelProperty(value = "计划首批下单量", index = 52)
    @FieldValid(fieldName = "计划首批下单量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String planOrderQtyStr;

    /**
     * 首批下单时间
     */
    //@ExcelProperty(value = "首批下单时间", index = 53)
    @FieldValid(fieldName = "首批下单时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String placeOrderTimeStr;

    /**
     * 预计首批到货时间
     */
    //@ExcelProperty(value = "预计首批到货时间", index = 54)
    @FieldValid(fieldName = "预计首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planArrivalTimeStr;

    /**
     * MOQ(最小起订量)
     */
    //@ExcelProperty(value = "MOQ(最小起订量)", index = 55)
    @FieldValid(fieldName = "MOQ(最小起订量)",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String moqStr;

    /**
     * 交货周期(天)
     */
    //@ExcelProperty(value = "交货周期(天)", index = 56)
    @FieldValid(fieldName = "交货周期(天)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String deliveryCycleStr;

    /**
     * 实际首批到货时间
     */
    //@ExcelProperty(value = "实际首批到货时间", index = 57)
    @FieldValid(fieldName = "实际首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String actualArrivalTimeStr;

    /**
     * 实际首批到货量
     */
    //@ExcelProperty(value = "实际首批到货量", index = 58)
    @FieldValid(fieldName = "实际首批到货量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String actualArrivalQtyStr;

    /**
     * 首批到货状态
     */
    //@ExcelProperty(value = "首批到货状态", index = 59)
    @FieldValid(fieldName = "首批到货状态",enumClass = PurchaseStateEnum.class)
    private String arrivalState;

    /**
     * 采购员
     */
    //@ExcelProperty(value = "采购员", index = 60)
    private String purchaseUser;

    /**
     * 一级供应商
     */
    //@ExcelProperty(value = "一级供应商", index = 61)
    private String mainSupplier;

    /**
     * 二级供应商
     */
    //@ExcelProperty(value = "二级供应商", index = 62)
    private String secondSupplier;

    /**
     * 量产入库时间
     */
    //@ExcelProperty(value = "量产入库时间", index = 63)
    @FieldValid(fieldName = "量产入库时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String firstMassProductDateStr;

    /**
     * 产品开发状态
     */
    //@ExcelProperty(value = "产品开发状态", index = 64)
    @FieldValid(fieldName = "产品开发状态",enumClass = ProductDetailStateEnum.class)
    private String productStateName;

    /**
     * 错误信息
     */
    //@ExcelProperty(value = "错误信息", index = 65)
    private String errorMsg;
}
