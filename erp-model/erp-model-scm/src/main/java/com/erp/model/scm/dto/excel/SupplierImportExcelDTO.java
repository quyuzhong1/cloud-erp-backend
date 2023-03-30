package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Lambda
 * @Classname SupplierImportExcelDTO
 * @Description TODO
 * @Date 2023-03-30 9:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierImportExcelDTO implements Serializable {


    /**
     * 供应商名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "供应商名称", index = 0)
    @FieldValid(fieldName = "供应商名称",isNotBlank = true)
    private String name;


    /**
     * 供应商等级
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "供应商等级", index = 1)
    @FieldValid(fieldName = "供应商等级",isNotBlank = true)
    private String gradeName;


    /**
     * 采购员
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "采购员", index = 2)
    @FieldValid(fieldName = "采购员",isNotBlank = true)
    private String purchaseUserName;


    /**
     * 公司地址
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "公司地址", index = 3)
    @FieldValid(fieldName = "公司地址",maxLength = 100)
    private String companyAddress;


    /**
     * 公司网址
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "公司网址", index = 4)
    @FieldValid(fieldName = "公司网址",maxLength = 100)
    private String companyWebsite;



    /**
     * 供应商状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "供应商状态", index = 5)
    @FieldValid(fieldName = "供应商状态",isNotBlank = true,fieldValues ="启用,未启用" )
    private String enabled;


    /**
     * 结算方式
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "结算方式", index = 6)
    private String payMethodName;


    /**
     * 结算付款币种
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "结算币种", index = 7)
    private String payCurrency;


    /**
     * 供应商分类
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "供应商分类", index = 8)
    private String categoryName;



    /**
     * 联系人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系人", index = 9)
    private String person;


    /**
     * 职务
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "职务", index = 10)
    private String position;


    /**
     * 联系人电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系人电话", index = 11)
    private String telNumber;


    /**
     * 联系人邮箱
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系人邮箱", index = 12)
    @FieldValid(fieldName = "联系人邮箱",maxLength = 50)
    private String email;


    /**
     * 是否默认联系人
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否默认联系人", index = 13)
    @FieldValid(fieldName = "是否默认联系人",fieldValues = "是,否")
    private String isDefault;


    /**
     * 联系人状态
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "联系人状态", index = 14)
    @FieldValid(fieldName = "联系人状态",fieldValues = "启用,未启用")
    private String contactEnabled;



    /**
     * 联系备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "联系备注", index = 15)
    @FieldValid(fieldName = "联系备注",maxLength = 255)
    private String contactRemark;



    /**
     * 账户名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "账户名称", index = 16)
    @FieldValid(fieldName = "账户名称",maxLength = 100)
    private String payee;


    /**
     * 银行名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "银行名称", index = 17)
    @FieldValid(fieldName = "银行名称",maxLength = 50)
    private String bankName;


    /**
     * 银行账号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "银行账号", index = 18)
    @FieldValid(fieldName = "银行账号",maxLength = 20)
    private String bankAccount;

    @ColumnWidth(30)
    @ExcelProperty(value = "开户行支行", index = 19)
    @FieldValid(fieldName = "开户行支行",maxLength = 255)
    private String bankSubbranch;


    @ColumnWidth(30)
    @ExcelProperty(value = "账户备注", index = 20)
    @FieldValid(fieldName = "账户备注",maxLength = 255)
    private String accountRemark;


    @ColumnWidth(20)
    @ExcelProperty(value = "资质名称", index = 21)
    @FieldValid(fieldName = "资质名称",maxLength = 50)
    private String credentialName;



    @ColumnWidth(20)
    @ExcelProperty(value = "资质有效期起", index = 22)
    private LocalDate effectiveDate;


    @ColumnWidth(20)
    @ExcelProperty(value = "资质有效期止", index = 23)
    private LocalDate expireDate;

    @ColumnWidth(30)
    @ExcelProperty(value = "资质备注", index = 24)
    @FieldValid(fieldName = "资质备注",maxLength = 255)
    private String credentialRemark;



    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =25 )
    private String  errorMsg;
}
