package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 仓库匹配
 * @author Will
 * @date: 2024/3/21 14:52
 */
@Data
public class TmsWarehouseMappingExcelDTO implements Serializable {


    /**
     * 仓库代码（物流商）
     */
    @ExcelProperty(value = "*仓库代码（物流商）")
    @FieldValid(fieldName = "仓库代码（物流商）",isNotBlank = true,maxLength = 200)
    private String  logisticsWarehouseCode;

    /**
     * 仓库名称（数大臣）
     */
    @ExcelProperty(value = "*仓库名称（数大臣）")
    @FieldValid(fieldName = "仓库名称（数大臣）",isNotBlank = true)
    private String  erpWarehouseName;

    /**
     * 错误信息
     */
    private String errorMsg;
}
