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
 * @description: TODO
 * @date 2022/11/24 11:35
 */
@Data
@NoArgsConstructor
public class ProductTaskViewInWarehouseTimeExcelDTO implements Serializable {

    @ColumnWidth(30)
    @ExcelProperty(value = "时间区间", index = 0)
    private String timeInterval;

    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 1)
    private String productName;

    @ColumnWidth(20)
    @ExcelProperty(value = "sku编号", index = 2)
    private String skuNo;

    @ColumnWidth(10)
    @ExcelProperty(value = "产品状态/项目状态", index = 3)
    private String statusName;

    @ColumnWidth(20)
    @ExcelProperty(value = "首批量产入库时间", index = 4)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String planListingTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "产品经理", index = 5)
    private String chargeName;
}
