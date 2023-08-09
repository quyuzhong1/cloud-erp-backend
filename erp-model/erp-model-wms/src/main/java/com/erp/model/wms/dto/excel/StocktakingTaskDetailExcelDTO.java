package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname StocktakingTaskDetailExcelDTO
 * @Description TODO
 * @Date 2023-08-09 12:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StocktakingTaskDetailExcelDTO {

    @ColumnWidth(30)
    @ExcelProperty(value = "盘点任务单号", index = 0)
    private String code;


    @ColumnWidth(30)
    @ExcelProperty(value = "仓库名称", index = 1)
    private String warehouseName;


    @ColumnWidth(30)
    @ExcelProperty(value = "仓位", index = 2)
    private String warehouseLocation;

    @ColumnWidth(30)
    @ExcelProperty(value = "sku", index = 3)
    private String skuNo;

    @ColumnWidth(30)
    @ExcelProperty(value = "盘点库存", index = 4)
    private Integer qty;
}
