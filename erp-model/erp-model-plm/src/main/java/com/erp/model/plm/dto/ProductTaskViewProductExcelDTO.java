package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 任务视图按产品导出DTO
 * @date 2022/11/24 11:25
 */
@Data
@NoArgsConstructor
public class ProductTaskViewProductExcelDTO implements Serializable {

    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 0)
    private String productName;

    @ColumnWidth(30)
    @ExcelProperty(value = "任务名称", index = 1)
    private String taskName;

    @ColumnWidth(20)
    @ExcelProperty(value = "计划开始日期", index = 2)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String planStartTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "计划结束日期", index = 3)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String planEndTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "实际开始日期", index = 4)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String realityStartTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "实际结束日期", index = 5)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String realityEndTime;

    @ColumnWidth(10)
    @ExcelProperty(value = "任务状态", index = 6)
    private String statusName;
}
