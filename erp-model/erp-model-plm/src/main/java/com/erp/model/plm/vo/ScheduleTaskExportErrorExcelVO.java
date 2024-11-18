package com.erp.model.plm.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname

 * @Date 2023-02-21 18:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleTaskExportErrorExcelVO  {



    @ExcelIgnore
    private String scheduleStatus;

    @ExcelIgnore
    private String taskId;

    @ExcelIgnore
    private String productId;

    @ColumnWidth(50)
    @ExcelProperty(value = "产品名称", index = 0)
    @FieldValid(fieldName = "产品名称",isNotBlank = true)
    private String productName;

    @ColumnWidth(50)
    @ExcelProperty(value = "任务名称", index = 1)
    @FieldValid(fieldName = "任务名称",isNotBlank = true)
    private String taskName;



    @ColumnWidth(50)
    @ExcelProperty(value = "任务状态", index = 2)
    private String statusName;


    /**
     * 计划开始时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划开始时间", index = 3)
    @FieldValid(fieldName = "计划开始时间",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planStartTime;

    /**
     * 计划结束时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划结束时间", index = 4)
    @FieldValid(fieldName = "计划结束时间",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planEndTime;




    @ColumnWidth(50)
    @ExcelProperty(value = "负责人名", index = 5)
    @FieldValid(fieldName = "负责人名",isNotBlank = true)
    private String chargeName;

    @ColumnWidth(100)
    @ExcelProperty(value = "错误信息", index = 6)
    private String errorMsg;

}
