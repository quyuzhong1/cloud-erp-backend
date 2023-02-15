package com.erp.model.plm.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ScheduleTaskExportExcelVO
 * @Description TODO
 * @Date 2023-02-10 11:44
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleTaskExportExcelVO  implements Serializable {

    @ColumnWidth(50)
    @ExcelProperty(value = "任务id", index = 0)
    private String taskId;


    @ColumnWidth(50)
    @ExcelProperty(value = "产品id", index = 1)
    private String productId;

    @ColumnWidth(50)
    @ExcelProperty(value = "产品名称", index = 2)
    private String productName;

    @ColumnWidth(50)
    @ExcelProperty(value = "任务名称", index = 3)
    private String taskName;


    @ExcelIgnore
    private String scheduleStatus;


    @ColumnWidth(50)
    @ExcelProperty(value = "任务状态", index = 4)
    private String scheduleStatusName;


    /**
     * 计划开始时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划开始时间", index = 5)
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划结束时间", index = 6)
    private Date planEndTime;




    @ColumnWidth(50)
    @ExcelProperty(value = "负责人名", index = 7)
    private String chargeName;

    @ColumnWidth(100)
    @ExcelProperty(value = "错误信息", index = 8)
    private String errorMsg;

}
