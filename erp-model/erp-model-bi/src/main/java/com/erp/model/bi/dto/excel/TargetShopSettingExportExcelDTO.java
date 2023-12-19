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
public class TargetShopSettingExportExcelDTO {

    @ColumnWidth(10)
    @ExcelProperty(value = "考核年份", index = 0)
    private String year;

    @ColumnWidth(20)
    @ExcelProperty(value = "考核部门", index = 1)
    private String deptName;

    @ColumnWidth(15)
    @ExcelProperty(value = "考核指标", index = 2)
    private String metricsName;

    @ColumnWidth(20)
    @ExcelProperty(value = "考核对象", index = 3)
    private String shopName;

    /**
     * 一月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "1月", index = 4)
    private BigDecimal january;

    /**
     * 二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "2月", index = 5)
    private BigDecimal february;

    /**
     * 三月
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "3月", index = 6)
    private BigDecimal march;


    /**
     * 四月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "4月", index = 7)
    private BigDecimal april;

    /**
     * 五月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "5月", index = 8)
    private BigDecimal may;


    /**
     * 六月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "6月", index = 9)
    private BigDecimal june;


    /**
     * 七月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "7月", index = 10)
    private BigDecimal july;


    /**
     * 八月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "8月", index = 11)
    private BigDecimal august;


    /**
     * 九月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "9月", index = 12)
    private BigDecimal september;


    /**
     * 十月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "10月", index = 13)
    private BigDecimal october;

    /**
     * 十一月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "11月", index = 14)
    private BigDecimal november;

    /**
     * 十二月值
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "12月", index = 15)
    private BigDecimal december;
    /**
     * 创建人
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "创建人", index = 16)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 17)
    private String createTime;
}
