package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname KingdeeBankAccountExcelDTO
 * @Date 2023-07-19 9:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class KingdeeBankAccountExcelDTO implements Serializable {


    /**
     * 银行账号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "银行账号", index = 4)
    @FieldValid(fieldName = "银行账号",isNotBlank = true,maxLength =50 )
    private String bankAccountNo;



    /**　
     * 开户银行
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "开户银行", index = 6)
    @FieldValid(fieldName = "开户银行",isNotBlank = true,maxLength =50 )
    private String bankType;
    /**　
     * 账户名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "银行账号", index = 7)
    @FieldValid(fieldName = "银行账号",isNotBlank = true,maxLength =50 )
    private String accountName;



    /**
     * 金蝶组织
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "使用组织", index = 9)
    @FieldValid(fieldName = "使用组织",isNotBlank = true,maxLength =50 )
    private String kindeeOrgCode;


    /**
     * 组织名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "使用组织名", index = 10)
    @FieldValid(fieldName = "使用组织名",isNotBlank = true,maxLength =50 )
    private String orgName;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =23)
    @ColumnWidth(30)
    private String  errorMsg;
}
