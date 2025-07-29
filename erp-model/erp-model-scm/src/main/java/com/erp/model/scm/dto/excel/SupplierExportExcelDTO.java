package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 导出的供应商
 * @author
 * @Classname SupplierExportExcelDTO

 * @Date 2023-03-29 14:07
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierExportExcelDTO implements Serializable {

    /**
     * 编号
     */
    @ExcelProperty(value = "供应商编号", index = 0)
    @ColumnWidth(20)
    private String  code;

    /**
     * 外部平台编号
     */
    private String voucherNo;

    /**
     * 名称
     */
    @ExcelProperty(value = "供应商名称", index = 1)
    @ColumnWidth(20)
    private String name;

    /**
     * 审核状态名
     */
    @ExcelProperty(value = "单据状态", index = 2)
    @ColumnWidth(10)
    private String approveStatusName;


    /**
     * 阶段名
     */
    @ExcelProperty(value = "供应商阶段", index = 3)
    @ColumnWidth(10)
    private String phaseName;


    /**
     * 供应商等级
     */
    @ExcelProperty(value = "供应商等级", index = 4)
    @ColumnWidth(20)
    private String gradeName;

    /**
     * 供应商分类
     */
    @ExcelProperty(value = "供应商分类", index = 5)
    @ColumnWidth(20)
    private String categoryName;


    /**
     * 启用状态
     */
    @ExcelProperty(value = "启用状态", index = 6)
    @ColumnWidth(10)
    private String enableStatus;


    /**
     * SRM协同 true 否 false 是
     */
    @ExcelProperty(value = "SRM协同", index = 7)
    @ColumnWidth(10)
    private String srmDisabled;

    /**
     * 订单接受规则
     */
    @ExcelProperty(value = "订单接受规则", index = 8)
    @ColumnWidth(10)
    private String orderAcceptRule;

    /**
     * 退货确认规则
     */
    @ExcelProperty(value = "退货确认规则", index = 9)
    @ColumnWidth(10)
    private String returnConfirmRule;

    /**
     * 结算方式
     */
    @ExcelProperty(value = "结算方式", index = 10)
    @ColumnWidth(30)
    private String payMethodName;

    /**
     * 付款条件
     */
    @ExcelProperty(value = "付款条件", index = 11)
    @ColumnWidth(30)
    private String paymentConditionName;

    /**
     * 采购员
     */
    @ExcelProperty(value = "采购员", index = 12)
    @ColumnWidth(10)
    private String purchaseUserName;


    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 13)
    @ColumnWidth(20)
    private String approveUserName;


    /**
     * 审核完成时间
     */
    @ExcelProperty(value = "审核完成时间", index = 14)
    @ColumnWidth(10)
    private LocalDateTime approveTime;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 15)
    @ColumnWidth(20)
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 16)
    @ColumnWidth(10)
    private String createUserName;

    /**
     * 联系人-人员
     */
    @ExcelProperty(value = "联系人-联系人", index = 17)
    private String person;
    /**
     * 联系人-职务
     */
    @ExcelProperty(value = "联系人-职务", index = 18)
    private String position;
    /**
     * 联系人-电话
     */
    @ExcelProperty(value = "联系人-电话", index = 19)
    private String telNumber;
    /**
     * 联系人-邮箱
     */
    @ExcelProperty(value = "联系人-邮箱", index = 20)
    private String email;
    /**
     * 联系人-默认联系人，是/否
     */
    @ExcelProperty(value = "联系人-默认联系人", index = 21)
    private String contactIsDefaultName;
    /**
     * 联系人-启用状态，是/否
     */
    @ExcelProperty(value = "联系人-启用状态", index = 22)
    private String contactDisabledName;
    /**
     * 联系人-备注
     */
    @ExcelProperty(value = "联系人-备注", index = 23)
    private String contactRemark;
    /**
     * 账户-账户名称
     */
    @ExcelProperty(value = "账户-账户名称", index = 24)
    private String payee;
    /**
     * 账户-收款银行
     */
    @ExcelProperty(value = "账户-收款银行", index = 25)
    private String bankName;
    /**
     * 账户-开户支行
     */
    @ExcelProperty(value = "开户支行", index = 26)
    private String bankSubbranch;
    /**
     * 账户-银行账号
     */
    @ExcelProperty(value = "账户-银行账号", index = 27)
    private String bankAccount;
    /**
     * 账户-支付方式
     */
    @ExcelProperty(value = "账户-支付方式", index = 28)
    private String bankPayMethodName;
    /**
     * 账户-是否默认,是/否
     */
    @ExcelProperty(value = "账户-是否默认", index = 29)
    private String accountDefaultName;
    /**
     * 账户-备注
     */
    @ExcelProperty(value = "账户-备注", index = 30)
    private String accountRemark;
    /**
     * 资质-名称
     */
    @ExcelProperty(value = "资质-名称", index = 31)
    private String credentialName;
    /**
     * 资质-有效期
     */
    @ExcelProperty(value = "资质-有效期", index = 32)
    private LocalDate effectiveDate;
    /**
     * 资质-失效期
     */
    @ExcelProperty(value = "资质-失效期", index = 33)
    private LocalDate expireDate;
    /**
     * 资质-证件名称
     */
    @ExcelProperty(value = "资质-证件名称", index = 34)
    private String attachmentName;
    /**
     * 资质-备注
     */
    @ExcelProperty(value = "资质-备注", index = 35)
    private String credentialRemark;

}
