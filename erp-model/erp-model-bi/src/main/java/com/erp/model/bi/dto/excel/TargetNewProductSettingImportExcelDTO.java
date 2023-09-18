package com.erp.model.bi.dto.excel;/**
 * @author Lambda
 * @Classname TargetStaffSettingImportExcelDTO
 * @Description TODO
 * @Date 2023-09-15 14:25
 * @Created by yl
 */

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-09-15 14:25
 */
@Data
@NoArgsConstructor
public class TargetNewProductSettingImportExcelDTO {


    @ColumnWidth(20)
    @ExcelProperty(value = "考核人员", index = 0)
    @FieldValid(fieldName = "考核人员", isNotBlank = true)
    private String staffName;

    @ColumnWidth(20)
    @ExcelProperty(value = "考核指标", index = 1)
    @FieldValid(fieldName = "考核指标", isNotBlank = true)
    private String metricsName;


    /**
     * 一月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "1月", index = 2)
    private BigDecimal january;

    /**
     * 一月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "1月占比", index = 3)
    private BigDecimal januaryRate;

    /**
     * 二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "2月", index = 4)
    private BigDecimal february;

    /**
     * 二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "2月占比", index = 5)
    private BigDecimal februaryRate;

    /**
     * 三月
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "3月", index = 6)
    private BigDecimal march;
    /**
     * 三月
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "3月占比", index = 7)
    private BigDecimal marchRate;


    /**
     * 四月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "4月", index = 8)
    private BigDecimal april;

    /**
     * 四月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "4月占比", index = 9)
    private BigDecimal aprilRate;
    /**
     * 五月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "5月", index = 10)
    private BigDecimal may;

    /**
     * 五月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "5月占比", index = 11)
    private BigDecimal mayRate;


    /**
     * 六月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "6月", index = 12)
    private BigDecimal june;

    /**
     * 六月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "6月占比", index = 13)
    private BigDecimal juneRate;


    /**
     * 七月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "7月", index = 14)
    private BigDecimal july;

    /**
     * 七月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "7月占比", index = 15)
    private BigDecimal julyRate;


    /**
     * 八月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "8月", index = 16)
    private BigDecimal august;

    /**
     * 八月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "8月占比", index = 17)
    private BigDecimal augustRate;


    /**
     * 九月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "9月", index = 18)
    private BigDecimal september;


    /**
     * 九月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "9月占比", index = 19)
    private BigDecimal septemberRate;

    /**
     * 十月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "10月", index = 20)
    private BigDecimal october;

    /**
     * 十月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "10月占比", index = 21)
    private BigDecimal octoberRate;


    /**
     * 十一月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "11月", index = 22)
    private BigDecimal november;


    /**
     * 十一月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "11月占比", index = 23)
    private BigDecimal novemberRate;

    /**
     * 十二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "12月", index = 24)
    private BigDecimal december;

    /**
     * 十二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "12月占比", index = 25)
    private BigDecimal decemberRate;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 26)
    private String errorMsg;
}
