package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: wtr
 * @Date: 2026/2/2 10:11
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@NoArgsConstructor
public class StocktakingTaskFirstQtyExcelDTO {

    @ColumnWidth(30)
    @ExcelProperty(value = "盘点任务单号", index = 0)
    @FieldValid(fieldName = "盘点任务单号",isNotBlank = true)
    private String code;


    @ColumnWidth(30)
    @ExcelProperty(value = "盘点方式", index = 1)
    @FieldValid(fieldName = "盘点方式")
    private String mode;

    @ColumnWidth(30)
    @ExcelProperty(value = "仓库", index = 2)
    @FieldValid(fieldName = "仓库",isNotBlank = true)
    private String warehouseName;


    @ColumnWidth(30)
    @ExcelProperty(value = "仓位", index = 3)
    private String warehouseLocation;

    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 4)
    @FieldValid(fieldName = "SKU",isNotBlank = true)
    private String skuNo;

    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 5)
    @FieldValid(fieldName = "产品名称")
    private String productName;

    @ColumnWidth(30)
    @ExcelProperty(value = "可用库存", index = 6)
    @FieldValid(fieldName = "可用库存")
    private String usableQty;

    @ColumnWidth(30)
    @ExcelProperty(value = "冻结库存", index = 7)
    @FieldValid(fieldName = "冻结库存")
    private String frozenQty;

    @ColumnWidth(30)
    @ExcelProperty(value = "*初盘数量", index = 8)
    @FieldValid(fieldName = "*初盘数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String firstQty;

    @ColumnWidth(30)
    @ExcelProperty(value = "盘点库存", index = 9)
    @FieldValid(fieldName = "盘点库存",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String qty;

    @ColumnWidth(30)
    @ExcelProperty(value = "差异数量", index = 10)
    @FieldValid(fieldName = "差异数量")
    private String diffQty;

    @ColumnWidth(30)
    @ExcelProperty(value = "盘点人", index = 11)
    @FieldValid(fieldName = "盘点人")
    private String stocktakingUserName;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =12)
    @ColumnWidth(50)
    private String  errorMsg;
}