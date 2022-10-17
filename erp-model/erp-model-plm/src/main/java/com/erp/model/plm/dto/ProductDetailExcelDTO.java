package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.alibaba.excel.annotation.ExcelProperty;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品sku信息导入
 * @Author Luo_WG
 * @Date 2022/9/27 16:23
 **/
@Data
@NoArgsConstructor
public class ProductDetailExcelDTO {

    @ExcelProperty( value = "skuNo", index = 0)
    private String skuNo;

    @ExcelProperty( value = "销售方式", index = 1)
    private String saleMethod;

    @ExcelProperty( value = "产品卖点", index = 2)
    private String sellSpot;

    @ExcelProperty( value = "产品功能描述", index = 3)
    private String functionDesc;

    @ExcelProperty( value = "产品用途", index = 4)
    private String usageDesc;

    @ExcelProperty( value = "存在侵权风险", index = 5)
    private String pirateRisk;

    @ExcelProperty(value = "主要材质", index = 6)
    private String materials;

    @ExcelProperty(value = "品名", index = 7)
    private String name;

    @ExcelProperty(value = "属性", index = 8)
    private String property;

    @ExcelProperty(value = "品牌", index = 9)
    private String brandName;

    @ExcelProperty(value = "产品属性", index = 10)
    private String variantProperty;

    @ExcelProperty(value = "计划上市时间", index = 11)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planListingTime;

    @ExcelProperty(value = "单位", index = 12)
    private String unitName;

    @ExcelProperty(value = "产品经理", index = 13)
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
    private Integer yearSaleQty;

    @ExcelProperty(value = "年目标销售额", index = 23)
    private BigDecimal yearSaleAmount;

    @ExcelProperty(value = "月目标销售量", index = 24)
    private Integer monthSaleQty;

    @ExcelProperty(value = "月目标销售额", index = 25)
    private BigDecimal monthSaleAmount;

    @ExcelProperty(value = "销售国家", index = 26)
    private String saleCountry;

    @ExcelProperty(value = "上市时间", index = 27)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date listingTime;

    @ExcelProperty(value = "退市时间", index = 28)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date delistingTime;

    @ExcelProperty(value = "图片是否完成", index = 29)
    private Integer isFinishedImg;

    @ExcelProperty(value = "视频是否完成", index = 30)
    private Integer isFinishedVideo;

    @ExcelProperty(value = "销售状态", index = 31)
    private Integer saleState;

    @ExcelProperty(value = "产品属性", index = 32)
    private String productProperty;

    @ExcelProperty(value = "报关中文名", index = 33)
    private String declareChineseName;

    @ExcelProperty(value = "报关英文名", index = 34)
    private String declareEnglishName;

    @ExcelProperty(value = "报关申报价格", index = 35)
    private BigDecimal declarePrice;

    @ExcelProperty(value = "海关编码", index = 36)
    private String customsCode;

    @ExcelProperty(value = "申报要素", index = 37)
    private String declareElement;

    @ExcelProperty(value = "英文材质", index = 38)
    private String englishMaterial;

    @ExcelProperty(value = "英文用途", index = 39)
    private String englishUsage;

    @ExcelProperty(value = "产品尺寸", index = 40)
    private String productSize;

    @ExcelProperty(value = "毛重", index = 41)
    private BigDecimal grossWeight;

    @ExcelProperty(value = "净重", index = 42)
    private BigDecimal netWeight;

    @ExcelProperty(value = "箱规", index = 43)
    private String boxSize;

    @ExcelProperty(value = "单箱重量", index = 44)
    private BigDecimal boxWeight;

    @ExcelProperty(value = "单箱数量", index = 45)
    private BigDecimal boxQty;

    @ExcelProperty(value = "错误信息", index = 46)
    private String errorMsg;
}
