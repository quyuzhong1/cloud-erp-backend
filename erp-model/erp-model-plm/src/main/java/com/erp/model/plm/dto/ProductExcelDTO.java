package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ProductExcelDTO
 * @Description TODO
 * @Date 2022-09-28 18:40
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductExcelDTO implements Serializable {


    @ColumnWidth(20)
    @ExcelProperty(value = "产品编号", index = 0)
    private String productId;

    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 1)
    private String productName;

    @ColumnWidth(20)
    @ExcelProperty(value = "产品负责人", index = 2)
    private String productChargeName;


    @ColumnWidth(20)
    @ExcelProperty(value = "项目负责人", index = 3)
    private String projectChargeName;

    @ColumnWidth(20)
    @ExcelProperty(value = "预计开始时间", index = 4)
    private String planStartTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "预计结束时间", index = 5)
    private String planEndTime;

    @ColumnWidth(10)
    @ExcelProperty(value = "产品状态", index = 6)
    private String productStatus;

    @ColumnWidth(10)
    @ExcelProperty(value = "项目状态", index = 7)
    private String projectStatus;

    @ColumnWidth(10)
    @ExcelProperty(value = "项目任务数量", index = 8)
    private Integer taskCount;

    @ColumnWidth(10)
    @ExcelProperty(value = "任务完成数量", index = 9)
    private Integer taskFinishCount;


}
