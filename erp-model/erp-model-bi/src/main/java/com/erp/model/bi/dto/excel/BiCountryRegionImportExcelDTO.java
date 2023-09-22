package com.erp.model.bi.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 区域/国家销售额导出
 * @Author Jim
 * @Date 2023-09-21 14:25
 */
@Data
@NoArgsConstructor
public class BiCountryRegionImportExcelDTO {


    @ColumnWidth(20)
    @ExcelProperty(value = "国家中文名称", index = 0)
    @FieldValid(fieldName = "国家中文名称", isNotBlank = true)
    private String countryNameCn;

    @ColumnWidth(20)
    @ExcelProperty(value = "国家英文名称", index = 1)
    @FieldValid(fieldName = "国家英文名称", isNotBlank = true)
    private String countryNameEn;

    @ColumnWidth(20)
    @ExcelProperty(value = "国家代号", index = 2)
    @FieldValid(fieldName = "国家代号", isNotBlank = true)
    private String countryCode;

    @ColumnWidth(20)
    @ExcelProperty(value = "国家所属区域名称", index = 3)
    @FieldValid(fieldName = "国家所属区域名称", isNotBlank = true)
    private String subregionName;

    @ColumnWidth(15)
    @ExcelProperty(value = "销售额(CNY)", index = 4)
    private BigDecimal salesAmount;
}
