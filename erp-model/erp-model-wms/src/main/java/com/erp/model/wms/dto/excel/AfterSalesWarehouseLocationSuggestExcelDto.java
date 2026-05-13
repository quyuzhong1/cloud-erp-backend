package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 仓位售后推荐导入
 *
 * @author liuchao
 * @date 2026-05-06
 */
@Data
public class AfterSalesWarehouseLocationSuggestExcelDto {

    /**
     * sku编码
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "sku编码", index = 0)
    private String skuNo;
    @ExcelIgnore
    private String skuId;

    /**
     * 所属库区
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "EAN码", index = 1)
    private String eanCode;

    /**
     * 产品名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 2)
    private String productName;

    /**
     * 所属仓库名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "所属仓库名称", index = 3)
    private String warehouseName;
    @ExcelIgnore
    private String warehouseId;

    /**
     * 所属库区
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "所属库区名称", index = 4)
    private String warehouseAreaName;
    @ExcelIgnore
    private String warehouseAreaCode;
    @ExcelIgnore
    private String warehouseAreaId;

    /**
     * 推荐仓位名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "推荐仓位编码", index = 5)
    private String suggestWarehouseLocationCode;
    @ExcelIgnore
    private String suggestWarehouseLocationId;

    /**
     * 优先级
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "优先级", index = 6)
    private String sort;

    /**
     * 启用状态（启用/禁用）
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "状态", index = 7)
    private String status;
    @ExcelIgnore
    private Boolean disabled;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 8)
    private String errorMsg;
}
