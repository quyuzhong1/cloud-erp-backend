package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 产品违禁词检测报告
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductForbiddenWordCheckReportExcelDTO implements Serializable {

    @ExcelProperty("SKU")
    private String skuNo;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("违禁词")
    private String forbiddenWord;

    @ExcelProperty("状态")
    private String status;
}
