package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 截单开船导入明细
 * @author jack
 * @date: 2025-07-18
 */
@Data
public class TmsCfgSailingExcelDTO implements Serializable {


    /**
     * 物流商
     */
    @ExcelProperty(value = "*物流商", index = 0)
    @FieldValid(fieldName = "*物流商",isNotBlank = true)
    private String  logisticsSupplierName;
    /**
     * 物流渠道
     */
    @ExcelProperty(value = "*物流渠道", index = 1)
    @FieldValid(fieldName = "*物流渠道",isNotBlank = true)
    private String  logisticsChannelName;
    /**
     * 截单周期
     */
    @ExcelProperty(value = "*截单周期", index = 2)
    @FieldValid(fieldName = "*截单周期",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer  dateValue;
    /**
     * 截单周期单位
     */
    @ExcelProperty(value = "*截单周期单位", index = 3)
    @FieldValid(fieldName = "*截单周期单位",isNotBlank = true)
    private String dateTypeName;
    /**
     * 截单日
     */
    @ExcelProperty(value = "*截单日", index = 4)
    @FieldValid(fieldName = "*截单日",isNotBlank = true)
    private String endDateName;
    /**
     * 截单时间
     */
    @ExcelProperty(value = "*截单时间", index = 5)
    @FieldValid(fieldName = "*截单时间",isNotBlank = true)
    private String endTime;
    /**
     * *开船日
     */
    @ExcelProperty(value = "*开船日", index = 6)
    @FieldValid(fieldName = "*开船日",isNotBlank = true)
    private String startDateName;
    /**
     * *开船时间
     */
    @ExcelProperty(value = "*开船时间", index = 7)
    @FieldValid(fieldName = "*开船时间",isNotBlank = true)
    private String startTime;
    /**
     * *起始日
     */
    @ExcelProperty(value = "*起始日", index = 8)
    @FieldValid(fieldName = "*起始日",isNotBlank = true)
    private String effectiveDate;
    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误数据", index = 9)
    private String errorMsg;
}
