package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 质检标准导入 Excel DTO
 * 
 * @author antigravity
 * @since 2026-03-24
 */
@Data
public class QcStandardImportExcelDTO {

    /**
     * SKU编号 (支持逗号分隔多个)
     */
    @ExcelProperty("SKU")
    private String sku;

    /**
     * 质检项目
     */
    @ExcelProperty("质检项目")
    private String inspectItemName;

    /**
     * 质检要求
     */
    @ExcelProperty("质检要求")
    private String inspectRequirement;
}
