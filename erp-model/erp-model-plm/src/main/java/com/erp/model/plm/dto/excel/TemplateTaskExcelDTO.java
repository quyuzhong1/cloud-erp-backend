package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class TemplateTaskExcelDTO implements Serializable {

    /**
     * 任务名称
     */
    @ExcelProperty(value = "*任务类型", index = 0)
    @FieldValid(fieldName = "任务类型", isNotBlank = true, maxLength = 50)
    private String type;

    /**
     * 任务类型
     */
    @ExcelProperty(value = "*任务名称", index = 1)
    @FieldValid(fieldName = "任务名称", isNotBlank = true)
    private String name;

    /**
     * 任务负责人
     */
    @ExcelProperty(value = "*任务负责人", index = 2)
    @FieldValid(fieldName = "任务负责人", isNotBlank = true)
    private String chargeName;

    /**
     * 是否固定任务
     */
    @ExcelProperty(value = "*是否固定任务", index = 3)
    @FieldValid(fieldName = "是否固定任务", isNotBlank = true)
    private String isFixed;

    /**
     * 前置任务
     */
    @ExcelProperty(value = "前置任务", index = 4)
    private String preTask;

    /**
     * 阶段名称
     */
    @ExcelProperty(value = "*阶段名称", index = 5)
    @FieldValid(fieldName = "阶段名称", isNotBlank = true)

    private String phaseName;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    @ExcelProperty(value = "任务优先级", index = 6)
    private String priority;

    /**
     * SKU关联
     */
    @ExcelProperty(value = "SKU关联", index = 7)
    private String refSku;

    /**
     * 计划开始时间
     */
    @ExcelProperty(value = "计划开始日期", index = 8)
    @FieldValid(fieldName = "计划开始日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planStartTime;

    /**
     * 计划结束时间
     */
    @ExcelProperty(value = "计划结束日期", index = 9)
    @FieldValid(fieldName = "计划结束日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planEndTime;

    /**
     * 工期
     */
    @ExcelProperty(value = "工期", index = 10)
    @FieldValid(fieldName = "工期",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private Integer workPeriod;

    /**
     * 目标交付文档
     */
    @ExcelProperty(value = "目标交付文档", index = 11)
    private String docsName;

    /**
     * 任务描述
     */
    @ExcelProperty(value = "任务描述", index = 12)
    private String description;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 13)
    private String errorMsg;

    public TemplateTaskExcelDTO() {
        this.workPeriod = 0;
    }
}
