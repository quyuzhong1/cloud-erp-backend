package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 仓位导入
 * @date 2024-05-31
 * @author tanmujin
 */
@Data
public class WarehouseLocationExcelDto{
    /**
     * 所属仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "所属仓库", index = 0)
    private String warehouseName;

    /**
     * 所属库区
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "所属库区", index = 1)
    private String warehouseAreaName;

    /**
     * 仓位编码
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓位编码", index = 2)
    private String warehouseLocationCode;

    /**
     * 仓位名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓位名称", index = 3)
    private String warehouseLocationName;

    /**
     * 备注
     */
    @ColumnWidth(40)
    @ExcelProperty(value = "备注", index = 4)
    private String remark;

    @ColumnWidth(30)
    @ExcelProperty(value = "错误信息", index = 5)
    private String errorMsg;
}
