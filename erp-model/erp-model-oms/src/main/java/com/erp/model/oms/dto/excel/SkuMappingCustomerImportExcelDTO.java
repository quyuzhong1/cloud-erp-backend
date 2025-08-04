package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class SkuMappingCustomerImportExcelDTO {

    @ColumnWidth(30)
    @ExcelProperty(value = "客户", index = 0)
    @FieldValid(fieldName = "客户", isNotBlank = true, maxLength = 100)
    private String customer;

    @ColumnWidth(50)
    @ExcelProperty(value = "客户sku", index = 1)
    @FieldValid(fieldName = "客户sku", isNotBlank = true, maxLength = 200)
    private String platformSkuNo;

    @ColumnWidth(50)
    @ExcelProperty(value = "客户产品名称", index = 2)
    @FieldValid(fieldName = "客户产品名称")
    private String platformSkuName;

    @ColumnWidth(50)
    @ExcelProperty(value = "产品SKU", index = 3)
    @FieldValid(fieldName = "产品SKU", isNotBlank = true, maxLength = 200)
    private String skuNo;
    /**
     * 产品sku
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "启用时间", index = 4)
    @FieldValid(fieldName = "启用时间")
    private String enabledTime;

    @ColumnWidth(100)
    @ExcelProperty(value = "错误数据", index = 5)
    private String errorMsg;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkuMappingCustomerImportExcelDTO that = (SkuMappingCustomerImportExcelDTO) o;
        return Objects.equals(customer, that.customer) && Objects.equals(platformSkuNo, that.platformSkuNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, platformSkuNo);
    }
}
