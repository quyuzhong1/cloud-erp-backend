package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname QcReportDetailImportExcelDTO
 * @Description TODO
 * @Date 2023-04-21 19:32
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcReportDetailImportExcelDTO {

    /**
     * 质检项
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检项", index = 0)
    @FieldValid(fieldName = "质检项", isNotBlank = true)
    private String qcReportName;


    /**
     * 质检内容
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检内容", index = 1)
    @FieldValid(fieldName = "质检内容", isNotBlank = true)
    private String qcReportContent;


    /**
     * 质检说明
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "质检说明", index = 2)
    @FieldValid(fieldName = "质检说明", isNotBlank = true, maxLength = 250)
    private String description;


    /**
     * 质检说明
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检结果", index = 3)
    @FieldValid(fieldName = "质检结果", isNotBlank = true)
    private String resultDict;


    /**
     * 质检说明
     */
    @ExcelProperty(value = "错误数据", index = 4)
    private String errorMsg;
}
