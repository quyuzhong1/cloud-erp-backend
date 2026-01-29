package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jack
 * @date 2026-01-21
 */
@Data
public class CfgLogisticsCostExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ExcelProperty(value = "*序号" , index = 0)
    @FieldValid(fieldName = "*序号",isNotBlank = true)
    private String  no;

    /**
     * 配置单据
     */
    @ExcelProperty(value = "*配置单据", index = 1)
    @FieldValid(fieldName = "*配置单据",isNotBlank = true)
    private String  businessTypeName;
    @ExcelIgnore
    private String  businessType;

    /**
     * 配置类型
     */
    @ExcelProperty(value = "*配置类型", index = 2)
    @FieldValid(fieldName = "*配置类型",isNotBlank = true)
    private String cfgTypeName;
    @ExcelIgnore
    private String cfgType;

    /**
     * 配置平台
     */
    @ExcelProperty(value = "*配置平台", index = 3)
    @FieldValid(fieldName = "*配置平台",isNotBlank = true)
    private String  dictPlatformName;
    @ExcelIgnore
    private String  dictPlatform;

    /**
     * 识别名称
     */
    @ExcelProperty(value = "*识别名称", index = 4)
    @FieldValid(fieldName = "*识别名称",isNotBlank = true)
    private String  name;

    /**
     * sheet名称
     */
    @ExcelProperty(value = "*sheet名称", index = 5)
    @FieldValid(fieldName = "*sheet名称",isNotBlank = true)
    private String  sheetName;

    /**
     * 行开始
     */
    @ExcelProperty(value = "*行开始", index = 6)
    @FieldValid(fieldName = "*行开始",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer  headerRow;

    /**
     * 费用来源
     */
    @ExcelProperty(value = "*费用来源", index = 7)
    @FieldValid(fieldName = "*费用来源",isNotBlank = true)
    private String  costTypeName;
    @ExcelIgnore
    private String  costType;

    /**
     * 导入处理（多个用英文逗号隔开）
     */
    @ExcelProperty(value = "*导入处理（多个用英文逗号隔开）", index = 8)
    @FieldValid(fieldName = "*导入处理（多个用英文逗号隔开）",isNotBlank = true)
    private String  importTypeName;
    @ExcelIgnore
    private String  importType;

    /**
     * 启用状态
     */
    @ExcelProperty(value = "*启用状态", index = 9)
    @FieldValid(fieldName = "*启用状态",isNotBlank = true)
    private String  disabledName;
    @ExcelIgnore
    private Boolean  disabled;

    /**
     * 物流商抬头字段
     */
    @ExcelProperty(value = "*物流商抬头字段", index = 10)
    @FieldValid(fieldName = "*物流商抬头字段",isNotBlank = true)
    private String  sourceField;

    /**
     * 物流商明细字段
     */
    @ExcelProperty(value = "物流商明细字段", index = 11)
    @FieldValid(fieldName = "物流商明细字段")
    private String  sourceDetailField;

    /**
     * 数大臣单据字段
     */
    @ExcelProperty(value = "*数大臣单据字段", index = 12)
    @FieldValid(fieldName = "数大臣单据字段",isNotBlank = true)
    private String  targetFieldName;
    @ExcelIgnore
    private String  targetFieldId;


    /**
     * 数大臣字段明细
     */
    @ExcelProperty(value = "数大臣字段明细", index = 13)
    @FieldValid(fieldName = "数大臣字段明细")
    private String  targetDetailFieldName;
    @ExcelIgnore
    private String  targetDetailFieldId;

    /**
     * 错误信息
     */
    private String errorMsg;
}
