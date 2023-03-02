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

    @ExcelProperty( value = "skuNo", index = 0)
    @FieldValid(fieldName = "sku", isNotBlank = true,maxLength = 10,formatPattern = FieldFormatPatternTypeEnum.NUMBER_LETTER)
    private String skuNo;

    @ExcelProperty( value = "产品分类", index = 1)
    @FieldValid(fieldName = "产品分类", isNotBlank = true)
    private String category;

    @ExcelProperty( value = "销售方式", index = 2)
    @FieldValid(fieldName = "销售方式",enumClass = SaleMethodEnum.class)
    private String saleMethod;

    @ExcelProperty( value = "产品卖点", index = 3)
    private String sellSpot;

    @ExcelProperty( value = "产品功能描述", index = 4)
    private String functionDesc;

    @ExcelProperty( value = "产品用途", index = 5)
    private String usageDesc;

    @ExcelProperty( value = "存在侵权风险", index = 6)
    @FieldValid(fieldName = "存在侵权风险",fieldValues = "是,否")
    private String pirateRisk;

    @ExcelProperty(value = "主要材质", index = 7)
    private String materials;

    @ExcelProperty(value = "品名", index = 8)
    @FieldValid(fieldName = "品名", isNotBlank = true,maxLength = 50)
    private String name;

    @ExcelProperty(value = "品牌", index = 9)
    @FieldValid(fieldName = "品牌", isNotBlank = true,maxLength = 250)
    private String brandName;

    @ExcelProperty(value = "产品属性", index = 10)
    private String property;

    @ExcelProperty(value = "计划上市时间", index = 11)
    @FieldValid(fieldName = "计划上市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planListingTimeStr;

    @ExcelProperty(value = "单位", index = 12)
    private String unitName;

    @ExcelProperty(value = "产品经理", index = 13)
    @FieldValid(fieldName = "产品经理", isNotBlank = true,maxLength = 20)
    private String chargeName;

    @ExcelProperty(value = "目标含税成本", index = 14)
    private String targetTaxCost;

    @ExcelProperty(value = "目标不含税成本", index = 15)
    private String targetNoTaxCost;

    @ExcelProperty(value = "实际含税成本", index = 16)
    private String actualTaxCost;

    @ExcelProperty(value = "实际不含税成本", index = 17)
    private String actualNoTaxCost;

    @ExcelProperty(value = "标准零售价", index = 18)
    private String retailPrice;

    @ExcelProperty(value = "目标毛利率", index = 19)
    private String targetGpm;

    @ExcelProperty(value = "实际毛利率(人民币)", index = 20)
    private String actualGpmCny;

    @ExcelProperty(value = "实际毛利率(美元)", index = 21)
    private String actualGpmUsd;

    @ExcelProperty(value = "年目标销售量", index = 22)
    @FieldValid(fieldName = "年目标销售量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String yearSaleQtyStr;

    @ExcelProperty(value = "年目标销售额", index = 23)
    @FieldValid(fieldName = "年目标销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String yearSaleAmountStr;

    @ExcelProperty(value = "月目标销售量", index = 24)
    @FieldValid(fieldName = "月目标销售量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String monthSaleQtyStr;

    @ExcelProperty(value = "月目标销售额", index = 25)
    @FieldValid(fieldName = "月目标销售额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String monthSaleAmountStr;

    @ExcelProperty(value = "销售国家", index = 26)
    private String saleCountry;

    @ExcelProperty(value = "上市时间", index = 27)
    @FieldValid(fieldName = "上市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String listingTimeStr;

    @ExcelProperty(value = "退市时间", index = 28)
    @FieldValid(fieldName = "退市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String delistingTimeStr;

    @ExcelProperty(value = "图片是否完成", index = 29)
    @FieldValid(fieldName = "图片是否完成",fieldValues = "是,否")
    private String isFinishedImg;

    @ExcelProperty(value = "视频是否完成", index = 30)
    @FieldValid(fieldName = "视频是否完成",fieldValues = "是,否")
    private String isFinishedVideo;

    @ExcelProperty(value = "销售状态", index = 31)
    @FieldValid(fieldName = "销售状态",enumClass = SaleStateEnum.class)
    private String saleState;

    @ExcelProperty(value = "报关产品属性", index = 32)
    private String productProperty;

    @ExcelProperty(value = "报关中文名", index = 33)
    private String declareChineseName;

    @ExcelProperty(value = "报关英文名", index = 34)
    private String declareEnglishName;

    @ExcelProperty(value = "报关申报价格", index = 35)
    @FieldValid(fieldName = "报关申报价格",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String declarePriceStr;

    @ExcelProperty(value = "海关编码", index = 36)
    private String customsCode;

    @ExcelProperty(value = "申报要素", index = 37)
    private String declareElement;

    @ExcelProperty(value = "英文材质", index = 38)
    private String englishMaterial;

    @ExcelProperty(value = "英文用途", index = 39)
    private String englishUsage;

    @ExcelProperty(value = "产品尺寸(长)", index = 40)
    @FieldValid(fieldName = "产品尺寸(长)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String productSizeLengthStr;

    @ExcelProperty(value = "产品尺寸(宽)", index = 41)
    @FieldValid(fieldName = "产品尺寸(宽)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String productSizeWideStr;

    @ExcelProperty(value = "产品尺寸(高)", index = 42)
    @FieldValid(fieldName = "产品尺寸(高)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String productSizeHighStr;

    @ExcelProperty(value = "毛重", index = 43)
    @FieldValid(fieldName = "毛重",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String grossWeightStr;

    @ExcelProperty(value = "净重", index = 44)
    @FieldValid(fieldName = "净重",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String netWeightStr;

    @ExcelProperty(value = "箱规(长)", index = 45)
    @FieldValid(fieldName = "箱规(长)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxSizeLengthStr;

    @ExcelProperty(value = "箱规(宽)", index = 46)
    @FieldValid(fieldName = "箱规(宽)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxSizeWideStr;

    @ExcelProperty(value = "箱规(高)", index = 47)
    @FieldValid(fieldName = "箱规(高)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxSizeHighStr;

    @ExcelProperty(value = "单箱重量", index = 48)
    @FieldValid(fieldName = "单箱重量",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxWeightStr;

    @ExcelProperty(value = "单箱数量", index = 49)
    @FieldValid(fieldName = "单箱数量",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxQtyStr;

    @ExcelProperty(value = "ean码", index = 50)
    private String ean;

    @ExcelProperty(value = "计划首批下单量", index = 51)
    @FieldValid(fieldName = "计划首批下单量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String planOrderQtyStr;

    @ExcelProperty(value = "首批下单时间", index = 52)
    @FieldValid(fieldName = "首批下单时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String placeOrderTimeStr;

    @ExcelProperty(value = "预计首批到货时间", index = 53)
    @FieldValid(fieldName = "预计首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planArrivalTimeStr;

    @ExcelProperty(value = "MOQ(最小起订量)", index = 54)
    @FieldValid(fieldName = "MOQ(最小起订量)",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String moqStr;

    @ExcelProperty(value = "交货周期(天)", index = 55)
    @FieldValid(fieldName = "交货周期(天)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String deliveryCycleStr;

    @ExcelProperty(value = "实际首批到货时间", index = 56)
    @FieldValid(fieldName = "实际首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String actualArrivalTimeStr;

    @ExcelProperty(value = "实际首批到货量", index = 57)
    @FieldValid(fieldName = "实际首批到货量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String actualArrivalQtyStr;

    @ExcelProperty(value = "首批到货状态", index = 58)
    @FieldValid(fieldName = "首批到货状态",enumClass = PurchaseStateEnum.class)
    private String arrivalState;

    @ExcelProperty(value = "采购员", index = 59)
    private String purchaseUser;

    @ExcelProperty(value = "一级供应商", index = 60)
    private String mainSupplier;

    @ExcelProperty(value = "二级供应商", index = 61)
    private String secondSupplier;

    @ExcelProperty(value = "量产入库时间", index = 62)
    @FieldValid(fieldName = "量产入库时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String firstMassProductDateStr;

    @ExcelProperty(value = "产品开发状态", index = 63)
    @FieldValid(fieldName = "产品开发状态",enumClass = ProductDetailStateEnum.class)
    private String productStateName;

    @ExcelProperty(value = "错误信息", index = 64)
    private String errorMsg;
}
