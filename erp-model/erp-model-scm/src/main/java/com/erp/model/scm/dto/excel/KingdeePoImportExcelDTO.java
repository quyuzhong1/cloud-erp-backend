package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶采购订单导入DTO
 * @date 2023/7/5 15:12
 */
@Data
public class KingdeePoImportExcelDTO {


    @ExcelProperty(value = "FBillHead(PUR_PurchaseOrder)")
    @FieldValid(fieldName = "单据序号", isNotBlank = true)

    private String  skuNo;


    @ExcelProperty(value = "FBillTypeID")
    @FieldValid(fieldName = "SKUs", isNotBlank = true)
    private String  skuNos;
}
