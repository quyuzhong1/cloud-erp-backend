package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname TaskExcelDTO
 * @Description TODO
 * @Date 2022-09-29 8:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskExcelDTO implements Serializable {

    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 0)
    private String productName;

    @ColumnWidth(20)
    @ExcelProperty(value = "任务id", index = 1)
    private String taskId;

    @ColumnWidth(30)
    @ExcelProperty(value = "任务名称", index = 2)
    private String taskName;

    @ColumnWidth(20)
    @ExcelProperty(value = "预计开始时间", index = 3)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String planStartTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "预计结束时间", index = 4)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String planEndTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "任务负责人", index = 5)
    private String chargeName;


    @ColumnWidth(20)
    @ExcelProperty(value = "实际开始时间", index = 6)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String realityStartTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "实际结束时间", index = 7)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String realityEndTime;

    @ColumnWidth(10)
    @ExcelProperty(value = "预计工期", index = 8)
    private String expectedDay;

    @ColumnWidth(10)
    @ExcelProperty(value = "任务状态", index = 9)
    private String taskState;



}
