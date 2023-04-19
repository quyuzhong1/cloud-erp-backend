package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/18 20:19
 */
@Data
@NoArgsConstructor
public class ExportQcDocumentExcelDTO {

    /**
     * 日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "日期", index = 0)
    private String qcDate;

    /**
     * 质检员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检员", index = 0)
    private String qcUserName;

    /**
     * 质检单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检单号", index = 0)
    private String qcCode;

    /**
     * 采购单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购单号", index = 0)
    private String purchaseOrderCode;

    /**
     * SKU
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 0)
    private String skuNo;

    /**
     * SKU名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU名称", index = 0)
    private String productName;

    /**
     * 质检数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检数量", index = 0)
    private String qcQty;

    /**
     * 质检状态
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检状态", index = 0)
    private String qcStatusName;

    /**
     * 质检结束时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检结束时间", index = 0)
    private String qcEndTime;

    /**
     * 质检耗时
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检耗时", index = 0)
    private String qcUseTime;

    /**
     * 质检预警
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检预警", index = 0)
    private String warnRemark;
}
