package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author jack
 * @Date 2025-06-26
 */
@Data
@NoArgsConstructor
public class SupplierVisitImportExcelDTO implements Serializable {


    /**
     * 供应商名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*供应商名称", index = 0)
    @FieldValid(fieldName = "供应商名称",isNotBlank = true,maxLength =50 )
    private String name;


    /**
     * 拜访类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*拜访类型", index = 1)
    @FieldValid(fieldName = "拜访类型",isNotBlank = true)
    private String visitTypeName;


    /**
     * 拜访时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*拜访时间(yyyy-MM-dd格式)", index = 2)
    @FieldValid(fieldName = "拜访时间",isNotBlank = true)
    private String visitTime;

    /**
     * 拜访人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*拜访人(多个请用英文逗号隔开)", index = 3)
    @FieldValid(fieldName = "拜访人",isNotBlank = true)
    private String peoples;


    /**
     * 拜访物料
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "拜访物料(多个请用英文逗号隔开)", index = 4)
    @FieldValid(fieldName = "拜访物料")
    private String skus;



    /**
     * 拜访记录
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*拜访记录", index = 5)
    @FieldValid(fieldName = "拜访记录",isNotBlank = true)
    private String content;


    /**
     * 拜访结果
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*拜访结果", index = 6)
    @FieldValid(fieldName = "拜访结果",isNotBlank = true)
    private String resultName;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =7)
    @ColumnWidth(50)
    private String  errorMsg;
}
