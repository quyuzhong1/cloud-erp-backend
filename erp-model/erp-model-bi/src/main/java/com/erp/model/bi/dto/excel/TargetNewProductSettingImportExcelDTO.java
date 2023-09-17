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
     * 二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "2月", index = 3)
    private BigDecimal february;

    /**
     * 三月
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "3月", index = 4)
    private BigDecimal march;


    /**
     * 四月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "4月", index = 5)
    private BigDecimal april;

    /**
     * 五月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "5月", index = 6)
    private BigDecimal may;


    /**
     * 六月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "6月", index = 7)
    private BigDecimal june;


    /**
     * 七月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "7月", index = 8)
    private BigDecimal july;


    /**
     * 八月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "8月", index = 9)
    private BigDecimal august;


    /**
     * 九月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "9月", index = 10)
    private BigDecimal september;


    /**
     * 十月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "10月", index = 11)
    private BigDecimal october;

    /**
     * 十一月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "11月", index = 12)
    private BigDecimal november;

    /**
     * 十二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "12月", index = 13)
    private BigDecimal december;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 14)
    private String errorMsg;
}
