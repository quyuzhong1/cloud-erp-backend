package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class LogisticsChannelConstraintExcelDTO {


    @ExcelIgnore
    private String channelId;

    @ExcelIgnore
    private String country;

    /**
     * 物流商
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "物流商", index = 0)
    @FieldValid(fieldName = "物流商", isNotBlank = true)
    private String logisticsProvider;

    /**
     * 渠道代码
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "渠道代码", index = 1)
    @FieldValid(fieldName = "渠道代码", isNotBlank = true)
    private String channelCode;


    /**
     * 国家名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "国家名称", index = 2)
    @FieldValid(fieldName = "国家名称", isNotBlank = true)
    private String countryName;


    /**
     * 包裹重量上限(g)
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "包裹重量上限(g)", index = 3)
    private BigDecimal maxWeight = BigDecimal.ZERO;

    /**
     * maxLength
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "超尺寸-长(CM)", index = 4)
    private BigDecimal maxLength = BigDecimal.ZERO;

    /**
     * maxWidth
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "超尺寸-宽(CM)", index = 5)
    private BigDecimal maxWidth = BigDecimal.ZERO;

    /**
     * maxHeight
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "超尺寸-高(CM)", index = 6)
    private BigDecimal maxHeight = BigDecimal.ZERO;

    /**
     * maxCustomsAmount
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "最高报关金额(USD)", index = 7)
    private BigDecimal maxCustomsAmount = BigDecimal.ZERO;

    /**
     * minCustomsAmount
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "最低报关金额(USD)", index = 8)
    private BigDecimal minCustomsAmount = BigDecimal.ZERO;

    /**
     * 错误信息
     */
    @ColumnWidth(100)
    @ExcelProperty(value = "错误数据", index = 9)
    private String errorMsg;

    public boolean isValid() {
        if ( minCustomsAmount.compareTo(BigDecimal.ZERO) == 0 && maxCustomsAmount.compareTo(BigDecimal.ZERO) == 0 &&
                maxWeight.compareTo(BigDecimal.ZERO) == 0 && maxLength.compareTo(BigDecimal.ZERO) == 0 &&
                maxWidth.compareTo(BigDecimal.ZERO) == 0 && maxHeight.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return true;
    }

    public boolean isValidSize() {
        if (maxLength.compareTo(BigDecimal.ZERO) > 0 && maxWidth.compareTo(BigDecimal.ZERO) > 0 && maxHeight.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        if (maxLength.compareTo(BigDecimal.ZERO) > 0 || maxWidth.compareTo(BigDecimal.ZERO) > 0 || maxHeight.compareTo(BigDecimal.ZERO) > 0) {
            return false;
        }
        return true;
    }

}
