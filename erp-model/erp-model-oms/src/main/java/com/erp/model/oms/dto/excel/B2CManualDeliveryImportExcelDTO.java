package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
public class B2CManualDeliveryImportExcelDTO {

    @ColumnWidth(20)
    @ExcelProperty(value = "*销售单号", index = 0)
    @FieldValid(fieldName = "*销售单号",isNotBlank = true)
    private String code;


    @ColumnWidth(20)
    @ExcelProperty(value = "*实际发货仓库", index = 1)
    @FieldValid(fieldName = "*实际发货仓库",isNotBlank = true)
    private String warehouseName;

    @ColumnWidth(20)
    @ExcelProperty(value = "*物流渠道", index = 2)
    @FieldValid(fieldName = "*物流渠道",isNotBlank = true)
    private String channelName;

    @ColumnWidth(20)
    @ExcelProperty(value = "*运单号", index = 3)
    @FieldValid(fieldName = "*运单号",isNotBlank = true)
    private String transportNo;

    @ColumnWidth(20)
    @ExcelProperty(value = "*实际发货时间", index = 4)
    @FieldValid(fieldName = "*实际发货时间",isNotBlank = true)
    private String deliveryTimeStr;

    @ExcelIgnore
    private LocalDateTime deliveryTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "实际发货单号", index = 5)
    @FieldValid(fieldName = "实际发货单号")
    private String actualDeliveryCode;

    @ColumnWidth(20)
    @ExcelProperty(value = "是否标发", index = 6)
    @FieldValid(fieldName = "是否标发",fieldValues = "是,否")
    private String platformShipFlag;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =7)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
