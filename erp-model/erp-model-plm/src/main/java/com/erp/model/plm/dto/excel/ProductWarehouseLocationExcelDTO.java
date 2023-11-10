package com.erp.model.plm.dto.excel;

import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ProductWarehouseLocationExcelDTO implements Serializable {

    /**
     * sku编号
     */
    @FieldValid(fieldName = "sku编号", isNotBlank = true, index = 0)
    private String skuNo;

    /**
     * 仓位
     */
    @FieldValid(fieldName = "仓位", isNotBlank = true, index = 1)
    private String warehouseLocation;

    /**
     * 错误信息
     */
    private String errorMsg;
}
