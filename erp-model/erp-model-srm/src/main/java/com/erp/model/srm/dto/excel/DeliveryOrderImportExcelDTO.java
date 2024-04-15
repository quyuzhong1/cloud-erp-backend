package com.erp.model.srm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.utils.LocalDateStringConverter;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.common.core.excel.LocalDateTimeConverter;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发货单导入
 */
@Data
public class DeliveryOrderImportExcelDTO implements Serializable {


    @ExcelProperty(value = "*订单单号")
    @FieldValid(fieldName = "订单单号",isNotBlank = true,maxLength = 32)
    private String sourceCode;


    @ExcelProperty(value = "*预计到货日期")
    @FieldValid(fieldName = "预计到货日期",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planDeliveryDate;

    @ExcelProperty(value = "*SKU")
    @FieldValid(fieldName = "SKU",isNotBlank = true)
    private String skuNo;

    @ExcelProperty(value = "*送货数量")
    @FieldValid(fieldName = "送货数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer deliveryQty;

    @ExcelProperty(value = "*赠品数量")
    @FieldValid(fieldName = "赠品数量",isNotBlank = true)
    private Integer giftQty;

    @ExcelProperty(value = "备注")
    private String remark;

    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
