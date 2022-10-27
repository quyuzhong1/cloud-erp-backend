package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.alibaba.excel.annotation.ExcelProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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

    @ExcelProperty(value = "品牌", index = 8)
    private String brandName;

    @ExcelProperty(value = "产品属性", index = 9)
    private String property;

    @ExcelProperty(value = "计划上市时间", index = 10)
    private Date planListingTime;

    @ExcelProperty(value = "单位", index = 11)
    private String unitName;

    @ExcelProperty(value = "产品经理", index = 12)
    private String chargeName;

    @ExcelProperty(value = "目标含税成本", index = 13)
    private String targetTaxCost;

    @ExcelProperty(value = "目标不含税成本", index = 14)
    private String targetNoTaxCost;

    @ExcelProperty(value = "实际含税成本", index = 15)
    private String actualTaxCost;

    @ExcelProperty(value = "实际不含税成本", index = 16)
    private String actualNoTaxCost;

    @ExcelProperty(value = "标准零售价", index = 17)
    private String retailPrice;

    @ExcelProperty(value = "目标毛利率", index = 18)
    private String targetGpm;

    @ExcelProperty(value = "实际毛利率(人民币)", index = 19)
    private String actualGpmCny;

    @ExcelProperty(value = "实际毛利率(美元)", index = 20)
    private String actualGpmUsd;

    @ExcelProperty(value = "年目标销售量", index = 21)
    private Long yearSaleQty;

    @ExcelProperty(value = "年目标销售额", index = 22)
    private BigDecimal yearSaleAmount;

    @ExcelProperty(value = "月目标销售量", index = 23)
    private Long monthSaleQty;

    @ExcelProperty(value = "月目标销售额", index = 24)
    private BigDecimal monthSaleAmount;

    @ExcelProperty(value = "销售国家", index = 25)
    private String saleCountry;

    @ExcelProperty(value = "上市时间", index = 26)
    private Date listingTime;

    @ExcelProperty(value = "退市时间", index = 27)
    private Date delistingTime;

    @ExcelProperty(value = "图片是否完成", index = 28)
    private String isFinishedImg;

    @ExcelProperty(value = "视频是否完成", index = 29)
    private String isFinishedVideo;

    @ExcelProperty(value = "销售状态", index = 30)
    private String saleState;

    @ExcelProperty(value = "报关产品属性", index = 31)
    private String productProperty;

    @ExcelProperty(value = "报关中文名", index = 32)
    private String declareChineseName;

    @ExcelProperty(value = "报关英文名", index = 33)
    private String declareEnglishName;

    @ExcelProperty(value = "报关申报价格", index = 34)
    private BigDecimal declarePrice;

    @ExcelProperty(value = "海关编码", index = 35)
    private String customsCode;

    @ExcelProperty(value = "申报要素", index = 36)
    private String declareElement;

    @ExcelProperty(value = "英文材质", index = 37)
    private String englishMaterial;

    @ExcelProperty(value = "英文用途", index = 38)
    private String englishUsage;

    @ExcelProperty(value = "产品尺寸", index = 39)
    private String productSize;

    @ExcelProperty(value = "毛重", index = 40)
    private BigDecimal grossWeight;

    @ExcelProperty(value = "净重", index = 41)
    private BigDecimal netWeight;

    @ExcelProperty(value = "箱规", index = 42)
    private String boxSize;

    @ExcelProperty(value = "单箱重量", index = 43)
    private BigDecimal boxWeight;

    @ExcelProperty(value = "单箱数量", index = 44)
    private BigDecimal boxQty;

    @ExcelProperty(value = "ean码", index = 45)
    private String ean;

    @ExcelProperty(value = "计划首批下单量", index = 46)
    private Integer planOrderQty;

    @ExcelProperty(value = "首批下单时间", index = 47)
    private Date placeOrderTime;

    @ExcelProperty(value = "预计首批到货时间", index = 48)
    private Date planArrivalTime;

    @ExcelProperty(value = "MOQ(最小起订量)", index = 49)
    private Integer moq;

    @ExcelProperty(value = "交货周期(天)", index = 50)
    private BigDecimal deliveryCycle;

    @ExcelProperty(value = "实际首批到货时间", index = 51)
    private Date actualArrivalTime;

    @ExcelProperty(value = "实际首批到货量", index = 52)
    private Integer actualArrivalQty;

    @ExcelProperty(value = "首批到货状态", index = 53)
    private String arrivalState;

    @ExcelProperty(value = "采购员", index = 54)
    private String purchaseUser;

    @ExcelProperty(value = "一级供应商", index = 55)
    private String mainSupplier;

    @ExcelProperty(value = "二级供应商", index = 56)
    private String secondSupplier;

    @ExcelProperty(value = "错误信息", index = 57)
    private String errorMsg;
}
