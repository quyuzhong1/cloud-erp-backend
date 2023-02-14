package com.erp.model.plm.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ChangeScheduleExportVO
 * @Description TODO
 * @Date 2023-02-14 10:53
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ChangeScheduleExportVO implements Serializable {

    @ColumnWidth(50)
    @ExcelProperty(value = "任务id", index = 0)
    private String taskId;


    @ColumnWidth(50)
    @ExcelProperty(value = "任务名称", index = 1)
    private String taskName;

    @ColumnWidth(50)
    @ExcelProperty(value = "负责人名", index = 2)
    private String chargeName;




    /**
     * 计划开始时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划开始时间(原)", index = 3)
    private Date originStartTime;

    /**
     * 计划结束时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划结束时间（原）", index = 4)
    private Date originEndTime;


    /**
     * 计划开始时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划开始时间", index = 5)
    private Date changeStartTime;

    /**
     * 计划结束时间
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "计划结束时间", index = 6)
    private Date changeEndTime;



    @ExcelIgnore
    private Integer status;

    @ColumnWidth(50)
    @ExcelProperty(value = "任务状态", index = 7)
    private String statusName;

    @ColumnWidth(100)
    @ExcelProperty(value = "错误信息", index = 8)
    private String errorMsg;

}
