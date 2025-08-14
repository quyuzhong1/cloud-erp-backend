package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 自发货费用
 * @date 2023/11/14 15:47
 */
@Data
public class DictHsCodeExcelDTO implements Serializable {

    /**
     * 报关中文名
     */
    @ExcelProperty(value = "*报关中文名" , index = 0)
    @FieldValid(fieldName = "*报关中文名",isNotBlank = true ,maxLength = 200)
    private String  description;

    /**
     * 中国海关编码
     */
    @ExcelProperty(value = "*中国海关编码", index = 1)
    @FieldValid(fieldName = "中国海关编码",isNotBlank = true,maxLength = 50)
    private String  hsCode;

    /**
     * 出口退税率（%）
     */
    @ExcelProperty(value = "*出口退税率（%）", index = 2)
    @FieldValid(fieldName = "*出口退税率（%）",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private String exportRebateRate;

    /**
     * *报关单位
     */
    @ExcelProperty(value = "*报关单位", index = 3)
    @FieldValid(fieldName = "*报关单位",isNotBlank = true)
    private String  firstDeclareUnit;

    /**
     * 类型
     */
    @ExcelProperty(value = "*申报要素", index = 4)
    @FieldValid(fieldName = "*申报要素",isNotBlank = true)
    private String  declareElement;

    /**
     * 错误信息
     */
    private String errorMsg;
}
