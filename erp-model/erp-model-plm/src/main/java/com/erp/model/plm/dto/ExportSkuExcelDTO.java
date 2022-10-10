package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ExportSkuExcelDTO implements Serializable {
    @ExcelProperty(value = "spu", index = 0)
    private String spuNo;

    @ExcelProperty(value = "产品名称", index = 1)
    private String name;

    @ExcelProperty(value = "销售方式", index = 2)
    private String saleMethod;

    @ExcelProperty(value = "产品类别", index = 3)
    private String category;

    @ExcelProperty(value = "产品卖点", index = 4)
    private String sellSpot;

    @ExcelProperty(value = "产品功能描述", index = 5)
    private String functionDesc;

    @ExcelProperty(value = "产品用途", index = 6)
    private String usageDesc;

    @ExcelProperty(value = "存在侵权风险", index = 7)
    private String pirateRisk;

    @ExcelProperty(value = "主要材质", index = 8)
    private String materials;

    @ExcelProperty(value = "规格", index = 9)
    private String property;

    @ExcelProperty(value = "品牌", index = 10)
    private String brandName;

    @ExcelProperty(value = "变体规格", index = 11)
    private String variantProperty;

    @ExcelProperty(value = "sku编号", index = 12)
    private String skuNo;

    @ExcelProperty(value = "sku名称", index = 13)
    private String skuName;

    @ExcelProperty(value = "计划上市时间", index = 14)
    private String planListingTime;

    @ExcelProperty(value = "单位名称", index = 15)
    private String unitName;

    @ExcelProperty(value = "产品经理", index = 16)
    private String chargeName;

    @ExcelProperty(value = "产品状态", index = 17)
    private String productState;

    @ExcelProperty(value = "目标含税成本", index = 18)
    private BigDecimal targetTaxCost;

    @ExcelProperty(value = "目标不含税成本", index = 19)
    private BigDecimal targetNoTaxCost;

    @ExcelProperty(value = "实际含税成本", index = 20)
    private BigDecimal actualTaxCost;

    @ExcelProperty(value = "实际不含税成本", index = 21)
    private String actualNoTaxCost;

    @ExcelProperty(value = "标准零售价", index = 22)
    private String retailPrice;

    @ExcelProperty(value = "目标毛利率", index = 23)
    private String targetGpm;

    @ExcelProperty(value = "实际毛利率（人民币）", index = 24)
    private String actualGpmCny;

    @ExcelProperty(value = "实际毛利率（美元）", index = 25)
    private String actualGpmUsd;

    @ExcelProperty(value = "年目标销售量", index = 26)
    private String yearSaleQty;

    @ExcelProperty(value = "年目标销售额", index = 27)
    private String yearSaleAmount;

    @ExcelProperty(value = "月目标销售量", index = 28)
    private String monthSaleQty;

    @ExcelProperty(value = "月目标销售额", index = 29)
    private String monthSaleAmount;

    @ExcelProperty(value = "销售国家", index = 30)
    private String saleCountry;

    @ExcelProperty(value = "上市时间", index = 31)
    private String listingTime;

    @ExcelProperty(value = "退市时间", index = 32)
    private String delistingTime;

    @ExcelProperty(value = "图片是否完成", index = 33)
    private String isFinishedImg;

    @ExcelProperty(value = "视频是否完成", index = 34)
    private String isFinishedVideo;

    @ExcelProperty(value = "销售状态", index = 35)
    private String saleState;

    @ExcelProperty(value = "产品尺寸", index = 36)
    private String productSize;

    @ExcelProperty(value = "毛重", index = 37)
    private String grossWeight;

    @ExcelProperty(value = "净重", index = 38)
    private String netWeight;

    @ExcelProperty(value = "箱规", index = 39)
    private String boxSize;

    @ExcelProperty(value = "单箱重量", index = 40)
    private String boxWeight;

    @ExcelProperty(value = "单箱数量", index = 41)
    private String boxQty;

    @ExcelProperty(value = "产品属性", index = 42)
    private String productProperty;

    @ExcelProperty(value = "报关中文名", index = 43)
    private String declareChineseName;

    @ExcelProperty(value = "报关英文名", index = 44)
    private String declareEnglishName;

    @ExcelProperty(value = "报关申报价格", index = 45)
    private String declarePrice;

    @ExcelProperty(value = "海关编码", index = 46)
    private String customsCode;

    @ExcelProperty(value = "申报要素", index = 47)
    private String declareElement;

    @ExcelProperty(value = "英文材质", index = 48)
    private String englishMaterial;

    @ExcelProperty(value = "英文用途", index = 49)
    private String englishUsage;
}
