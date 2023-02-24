package com.erp.model.plm.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ScheduleTaskExportExcelVO
 * @Description TODO
 * @Date 2023-02-10 11:44
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleTaskExportExcelVO  implements Serializable {



    @ExcelIgnore
    private String scheduleStatus;

    @ExcelIgnore
    private String taskId;

    @ExcelIgnore
    private String productId;

    @ColumnWidth(50)
    @ExcelProperty(value = "产品名称", index = 0)
    private String productName;

    @ColumnWidth(50)
    @ExcelProperty(value = "任务名称", index = 1)
    private String taskName;



    @ColumnWidth(50)
    @ExcelProperty(value = "任务状态", index = 2)
    private String statusName;


    /**
     * 计划开始时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划开始时间", index = 3)
    @FieldValid(fieldName = "计划开始时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planStartTime;

    /**
     * 计划结束时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划结束时间", index = 4)
    @FieldValid(fieldName = "计划结束时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planEndTime;




    @ColumnWidth(50)
    @ExcelProperty(value = "负责人名", index = 5)
    private String chargeName;

}
