package com.erp.model.bi.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @description: 目标管理导入
 * @author Will
 * @date: 2022/12/21 18:04
 */
@Data
@NoArgsConstructor
public class BiTargetManagementImportExcelDTO implements Serializable {

    /**
     * 年份
     */
    @ExcelProperty(value = "年份", index = 0)
    private Integer year;

    /**
     * 平台
     */
    @ExcelProperty(value = "平台", index = 1)
    private String platformName;

    /**
     * 品类
     */
    @ExcelProperty(value = "品类", index = 2)
    private String category;

    /**
     * 销量/销售额
     */
    @ExcelProperty(value = "销量/销售额", index = 3)
    private String targetTypeName;

    /**
     * 新老品
     */
    @ExcelProperty(value = "新品/老品", index = 4)
    private String productTypeName;

    /**
     * 产品定位
     */
    @ExcelProperty(value = "产品定位", index = 5)
    private String productPosition;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 6)
    private String skuNo;

    /**
     * SPU
     */
    @ExcelProperty(value = "SPU", index = 7)
    private String spuNo;

    /**
     * 品名
     */
    @ExcelProperty(value = "品名", index = 8)
    private String productName;

    /**
     * 客单价
     */
    @ExcelProperty(value = "客单价", index = 9)
    private BigDecimal salePrice;

    /**
     * 一月
     */
    @ExcelProperty(value = "一月", index = 10)
    private BigDecimal january;

    /**
     * 二月
     */
    @ExcelProperty(value = "二月", index = 11)
    private BigDecimal february;

    /**
     * 三月
     */
    @ExcelProperty(value = "三月", index = 12)
    private BigDecimal march;

    /**
     * 四月
     */
    @ExcelProperty(value = "四月", index = 13)
    private BigDecimal april;

    /**
     * 五月
     */
    @ExcelProperty(value = "五月", index = 14)
    private BigDecimal may;

    /**
     * 六月
     */
    @ExcelProperty(value = "六月", index = 15)
    private BigDecimal june;

    /**
     * 七月
     */
    @ExcelProperty(value = "七月", index = 16)
    private BigDecimal july;

    /**
     * 八月
     */
    @ExcelProperty(value = "八月", index = 17)
    private BigDecimal august;

    /**
     * 九月
     */
    @ExcelProperty(value = "九月", index = 18)
    private BigDecimal september;

    /**
     * 十月
     */
    @ExcelProperty(value = "十月", index = 19)
    private BigDecimal october;

    /**
     * 十一月
     */
    @ExcelProperty(value = "十一月", index = 20)
    private BigDecimal november;

    /**
     * 十二月
     */
    @ExcelProperty(value = "十二月", index = 21)
    private BigDecimal december;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 22)
    private String errorMsg;
}
