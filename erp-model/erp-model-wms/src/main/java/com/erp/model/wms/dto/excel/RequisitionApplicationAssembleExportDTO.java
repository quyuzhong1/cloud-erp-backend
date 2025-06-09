package com.erp.model.wms.dto.excel;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 要货申请组装导出excel
 **/
@Data
@NoArgsConstructor
public class RequisitionApplicationAssembleExportDTO {
    /**
     * 要货申请单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "要货申请单号", index = 0)
    private String code;

    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 1)
    private String sku;

    @ColumnWidth(30)
    @ExcelProperty(value = "组装套数", index = 2)
    private Integer assembleQty;

    @ColumnWidth(30)
    @ExcelProperty(value = "子件SKU", index = 3)
    private String childSku;

    @ColumnWidth(30)
    @ExcelProperty(value = "BOM用量", index = 4)
    private Integer bomQty;

    @ColumnWidth(30)
    @ExcelProperty(value = "子件数量", index = 5)
    private Integer childQty;

}
