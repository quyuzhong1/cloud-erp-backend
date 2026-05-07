package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 仓位绑定导入
 */
@Data
public class WarehouseLocationMappingExcelDTO implements Serializable {

    @ColumnWidth(30)
    @ExcelProperty(value = "*仓库名称", index = 0)
    private String sysWarehouseName;

    @ColumnWidth(20)
    @ExcelProperty(value = "*第三方系统", index = 1)
    private String dictPlatformName;

    @ColumnWidth(30)
    @ExcelProperty(value = "*仓位编码", index = 2)
    private String sysWarehouseLocation;

    @ColumnWidth(30)
    @ExcelProperty(value = "*绑定仓位编码", index = 3)
    private String thirdWarehouseLocation;

    @ColumnWidth(50)
    @ExcelProperty(value = "错误信息", index = 4)
    private String errorMsg;
}
