package com.erp.model.fms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.time.LocalDate;

/**
 * 资产卡片导入Excel DTO
 * 
 * @author wuht
 * @date 2025-01-10
 */
@Data
public class AssetCardImportExcelDTO {

    /**
     * 序号
     */
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private Integer no;

    /**
     * 资产组织
     */
    @ExcelProperty(value = "*资产组织", index = 1)
    @FieldValid(fieldName = "*资产组织", isNotBlank = true)
    private String orgName;

    /**
     * 计量单位
     */
    @ExcelProperty(value = "*计量单位", index = 2)
    @FieldValid(fieldName = "*计量单位", isNotBlank = true)
    private String unit;

    /**
     * 资产类别
     */
    @ExcelProperty(value = "*资产类别", index = 3)
    @FieldValid(fieldName = "*资产类别", isNotBlank = true)
    private String type;

    /**
     * 资产状态
     */
    @ExcelProperty(value = "*资产状态", index = 4)
    @FieldValid(fieldName = "*资产状态", isNotBlank = true)
    private String status;

    /**
     * 变动方式
     */
    @ExcelProperty(value = "*变动方式", index = 5)
    @FieldValid(fieldName = "*变动方式", isNotBlank = true)
    private String changeMethod;

    /**
     * 资产名称
     */
    @ExcelProperty(value = "*资产名称", index = 6)
    @FieldValid(fieldName = "*资产名称", isNotBlank = true)
    private String name;

    /**
     * 开始使用日期（字符串格式）
     */
    @ExcelProperty(value = "*开始使用日期", index = 7)
    @FieldValid(fieldName = "*开始使用日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String startUseDateStr;

    /**
     * 开始使用日期
     */
    @ExcelIgnore
    private LocalDate startUseDate;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 8)
    private String remark;

    /**
     * 资产位置
     */
    @ExcelProperty(value = "*资产位置", index = 9)
    @FieldValid(fieldName = "*资产位置", isNotBlank = true)
    private String assetLocationName;

    /**
     * 数量
     */
    @ExcelProperty(value = "*数量", index = 10)
    @FieldValid(fieldName = "*数量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer qty;

    /**
     * 使用部门
     */
    @ExcelProperty(value = "*使用部门", index = 11)
    @FieldValid(fieldName = "*使用部门", isNotBlank = true)
    private String useDeptName;

    /**
     * 费用项目
     */
    @ExcelProperty(value = "*费用项目", index = 12)
    @FieldValid(fieldName = "*费用项目", isNotBlank = true)
    private String costType;

    /**
     * 明细备注
     */
    @ExcelProperty(value = "备注", index = 13)
    private String detailRemark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =14)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
