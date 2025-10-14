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

/**
 * @author jack
 * @Date 2025-10-11
 */
@Data
@NoArgsConstructor
public class MoldInfoImportExcelDTO implements Serializable {
    /**
     * 模具编号
     */
    @ExcelIgnore
    private String code;

    /**
     * 模具名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*模具名称", index = 0)
    @FieldValid(fieldName = "*模具名称",isNotBlank = true )
    private String name;


    /**
     * 模具标识
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具标识", index = 1)
    @FieldValid(fieldName = "*模具标识",isNotBlank = true)
    private String tagStr;
    @ExcelIgnore
    private String tag;
    /**
     * 项目编号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "项目编号", index = 2)
    @FieldValid(fieldName = "项目编号")
    private String projectCode;

    /**
     * 项目名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*项目名称", index = 3)
    @FieldValid(fieldName = "*项目名称",isNotBlank = true)
    private String projectName;

    /**
     * 产品经理
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*产品经理", index = 4)
    @FieldValid(fieldName = "*产品经理",isNotBlank = true)
    private String chargeName;
    @ExcelIgnore
    private String chargeId;


    /**
     * 项目经理
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "项目经理", index = 5)
    @FieldValid(fieldName = "项目经理")
    private String projectChargeName;
    @ExcelIgnore
    private String projectChargeId;

    /**
     * 模具分类
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*模具分类", index = 6)
    @FieldValid(fieldName = "*模具分类",isNotBlank = true)
    private String categoryName;
    @ExcelIgnore
    private String categoryId;


    /**
     * 模具类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具类型", index = 7)
    @FieldValid(fieldName = "*模具类型",isNotBlank = true)
    private String typeName;
    @ExcelIgnore
    private String type;


    /**
     * 模具穴数
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具穴数", index = 8)
    @FieldValid(fieldName = "*模具穴数",isNotBlank = true)
    private String moldHoles;


    /**
     * 模具长(mm)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具长(mm)", index = 9)
    @FieldValid(fieldName = "*模具长(mm)",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal productLength;


    /**
     * 模具宽(mm)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具宽(mm)", index = 10)
    @FieldValid(fieldName = "*模具宽(mm)",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal productWidth;


    /**
     * 模具高(mm)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具高(mm)", index = 11)
    @FieldValid(fieldName = "*模具高(mm)",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal productHeight;


    /**
     * 模具材质
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具材质", index = 12)
    @FieldValid(fieldName = "*模具材质",isNotBlank = true)
    private String materials;

    /**
     * 开模周期(自然日)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*开模周期(自然日)", index = 13)
    @FieldValid(fieldName = "*开模周期(自然日)",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer cycle;

    /**
     * 模具启用日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*模具启用日期", index = 14)
    @FieldValid(fieldName = "*模具启用日期",isNotBlank = true)
    private String activationDateStr;
    @ExcelIgnore
    private LocalDate activationDate;
    /**
     *供应商
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*供应商", index = 15)
    @FieldValid(fieldName = "*供应商",isNotBlank = true)
    private String supplierName;
    @ExcelIgnore
    private String supplierCode;
    @ExcelIgnore
    private String supplierId;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 16)
    @FieldValid(fieldName = "备注",maxLength = 200)
    private String remark;

    /**
     * 含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*含税单价", index = 17)
    @FieldValid(fieldName = "*含税单价",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private BigDecimal taxPrice;

    /**
     * 税率(%)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*税率(%)", index = 18 )
    @FieldValid(fieldName = "*税率(%)",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private BigDecimal rate;

    /**
     * 结算方式
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*结算方式", index = 19)
    @FieldValid(fieldName = "*结算方式",isNotBlank = true)
    private String payMethodName;
    @ExcelIgnore
    private String payMethodId;

    /**
     * 付款条件
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*付款条件", index = 20)
    @FieldValid(fieldName = "*付款条件",isNotBlank = true)
    private String paymentConditionName;
    @ExcelIgnore
    private String paymentCondition;
    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =21)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
