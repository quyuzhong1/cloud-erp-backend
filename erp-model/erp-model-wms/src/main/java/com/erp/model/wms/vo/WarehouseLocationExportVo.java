package com.erp.model.wms.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 仓位导出
 * @date 2024-06-02
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class WarehouseLocationExportVo implements Serializable {
    /**
     * 仓位编码
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "仓位编码", index = 0)
    private String code;

    /**
     * 仓位名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓位名称", index = 1)
    private String name;

    /**
     * 所属仓库编码
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "所属仓库", index = 2)
    private String warehouseCode;

    /**
     * 所属库区编码
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "所属库区", index = 3)
    private String warehouseAreaCode;

    /**
     * 仓位状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓位状态", index = 4)
    private String status;

    /**
     * 启用状态：false启用，true禁用
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "启用状态", index = 5)
    private String disabled;

    /**
     * 备注
     */
    @ColumnWidth(70)
    @ExcelProperty(value = "备注", index = 6)
    private String remark;

    /**
     * 更新人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "更新人", index = 7)
    private String updateUserName;

    /**
     * 更新时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "更新时间", index = 8)
    private String updateTime;

}
