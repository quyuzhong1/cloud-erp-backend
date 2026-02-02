package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname StocktakingTaskDetailExcelDTO
 * @Description
 * @Date 2023-08-09 12:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StocktakingTaskDetailExcelDTO {

    @ColumnWidth(30)
    @ExcelProperty(value = "盘点任务单号", index = 0)
    @FieldValid(fieldName = "盘点任务单号",isNotBlank = true)
    private String code;


    @ColumnWidth(30)
    @ExcelProperty(value = "仓库名称", index = 1)
    @FieldValid(fieldName = "仓库名称",isNotBlank = true)
    private String warehouseName;


    @ColumnWidth(30)
    @ExcelProperty(value = "仓位", index = 2)
    private String warehouseLocation;

    @ColumnWidth(30)
    @ExcelProperty(value = "sku", index = 3)
    @FieldValid(fieldName = "sku",isNotBlank = true)
    private String skuNo;

    @ColumnWidth(30)
    @ExcelProperty(value = "初盘数量", index = 4)
    @FieldValid(fieldName = "初盘数量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String firstQty;

    @ColumnWidth(30)
    @ExcelProperty(value = "盘点库存", index = 4)
    @FieldValid(fieldName = "盘点库存",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String qty;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =5)
    @ColumnWidth(50)
    private String  errorMsg;
}
