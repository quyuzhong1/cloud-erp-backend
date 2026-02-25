package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
public class ProductChangeImportExcelDTO implements Serializable {

    @ExcelIgnore
    private String code;

    /**
     * 变更日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*变更日期", index = 0)
    @FieldValid(fieldName = "*变更日期",isNotBlank = true)
    private String billDateStr;
    @ExcelIgnore
    private LocalDate billDate;

    /**
     * 变更SKU
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*变更SKU", index = 1)
    @FieldValid(fieldName = "*变更SKU",isNotBlank = true )
    private String skuNo;


    /**
     * 变更原因
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "变更原因", index = 2)
    @FieldValid(fieldName = "变更原因")
    private String reason;

    /**
     * 变更字段
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*变更字段", index = 3)
    @FieldValid(fieldName = "*变更字段",isNotBlank = true )
    private String field;

    /**
     * 变更新值
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*变更新值", index = 4)
    @FieldValid(fieldName = "*变更新值",isNotBlank = true)
    private String newValue;
    private Object newValueObj;
    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 5)
    @FieldValid(fieldName = "备注")
    private String remark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =6)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
