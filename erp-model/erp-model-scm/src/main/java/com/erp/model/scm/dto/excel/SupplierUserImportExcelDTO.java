package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 自发货费用
 * @date 2023/11/14 15:47
 */
@Data
public class SupplierUserImportExcelDTO implements Serializable {

    /**
     * 供应商名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "供应商名称", index = 0)
    @FieldValid(fieldName = "供应商名称",maxLength = 100)
    private String supplierName;
    /**
     * 用户名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "用户名", index = 1)
    @FieldValid(fieldName = "用户名",isNotBlank = true,maxLength =50 )
    private String userName;


    /**
     * 手机号码
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "手机号码", index = 2)
    @FieldValid(fieldName = "手机号码",isNotBlank = true)
    private String mobile;


    /**
     * 采购员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "邮箱", index = 3)
    @FieldValid(fieldName = "邮箱",formatPattern = FieldFormatPatternTypeEnum.MAILBOX)
    private String email;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =4)
    @ColumnWidth(50)
    private String  errorMsg;
}
