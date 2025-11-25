package com.erp.model.fms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产处置单导入Excel DTO
 * 
 * @author jack
 * @date 2025-11-03
 */
@Data
public class AssetDisposalImportExcelDTO {

    /**
     * 序号
     */
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;


    /**
     * 业务日期
     */
    @ExcelProperty(value = "*业务日期", index = 1)
    @FieldValid(fieldName = "*业务日期", isNotBlank = true)
    private String businessDateStr;
    @ExcelIgnore
    private LocalDate businessDate;

    /**
     * 资产组织
     */
    @ExcelProperty(value = "*资产组织", index = 2)
    @FieldValid(fieldName = "*资产组织", isNotBlank = true)
    private String assetOrgName;
    @ExcelIgnore
    private String assetOrgId;

    /**
     * 处置方式
     */
    @ExcelProperty(value = "*处置方式", index = 3)
    @FieldValid(fieldName = "*处置方式", isNotBlank = true)
    private String disposalMethodName;
    @ExcelIgnore
    private String disposalMethod;
    @ExcelIgnore
    private String sourceType;

    /**
     * 处置原因
     */
    @ExcelProperty(value = "处置原因", index = 4)
    @FieldValid(fieldName = "处置原因")
    private String reason;


    /**
     * 卡片编码
     */
    @ExcelProperty(value = "*卡片编码", index = 5)
    @FieldValid(fieldName = "*卡片编码", isNotBlank = true)
    private String sourceCode;
    @ExcelIgnore
    private String sourceId;

    /**
     * 处置币别
     */
    @ExcelProperty(value = "处置币别", index = 6)
    @FieldValid(fieldName = "处置币别")
    private String disposalCurrency;


    /**
     * 清理费用
     */
    @ExcelProperty(value = "清理费用", index = 7)
    @FieldValid(fieldName = "清理费用",formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal cleanupCost;

    /**
     * 残值收入（含税）
     */
    @ExcelProperty(value = "残值收入（含税）", index = 8)
    @FieldValid(fieldName = "残值收入（含税）",formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal residualValue;

    /**
     * 发票类型
     */
    @ExcelProperty(value = "发票类型", index = 9)
    @FieldValid(fieldName = "发票类型")
    private String invoiceTypeName;
    @ExcelIgnore
    private String invoiceType;


    /**
     * 税率(%)
     */
    @ExcelProperty(value = "税率(%)", index = 10)
    @FieldValid(fieldName = "税率(%)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal taxRate;


    /**
     * 资产编码
     */
    @ExcelProperty(value = "*资产编码", index = 11)
    @FieldValid(fieldName = "*资产编码", isNotBlank = true)
    private String assetCode;
    /**
     * 资产位置ID http://172.16.100.11:3002/project/163/interface/api/39465  /fms/assetLocation/drop/down/list
     */
    @ExcelIgnore
    private String assetLocationId;

    /**
     * 资产位置名称
     */
    @ExcelIgnore
    private String assetLocationName;
    /**
     * 来源明细ID
     */
    @ExcelIgnore
    private String assetDisposalDetailId;
    /**
     * 来源明细ID
     */
    @ExcelIgnore
    private String sourceDetailId;

    /**
     * 数量
     */
    @ExcelProperty(value = "*数量", index = 12)
    @FieldValid(fieldName = "*数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer qty;
    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =13)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
