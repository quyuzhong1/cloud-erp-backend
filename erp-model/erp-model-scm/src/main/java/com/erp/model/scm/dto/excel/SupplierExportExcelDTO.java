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
     * 供应商代码
     */
    @ExcelProperty(value = "供应商代码", index = 10)
    @ColumnWidth(10)
    private String identificationCode;

    /**
     * 公司注册资金（万）
     */
    @ExcelProperty(value = "公司注册资金（万）", index = 11)
    @ColumnWidth(10)
    private Integer registeredCapital;

    /**
     * 供应商属性名称
     */
    @ExcelProperty(value = "供应商属性", index = 12)
    @ColumnWidth(10)
    private String propertyNames;

    /**
     * 体系认证名称
     */
    @ExcelProperty(value = "体系认证", index = 13)
    @ColumnWidth(10)
    private String certificateNames;

    /**
     * 产品分类名称
     */
    @ExcelProperty(value = "产品分类", index = 14)
    @ColumnWidth(10)
    private String productCategoryNames;

    /**
     * 应用分类名称
     */
    @ExcelProperty(value = "应用分类", index = 15)
    @ColumnWidth(10)
    private String applicationCategoryNames;

    /**
     * 工厂所在地名称
     */
    @ExcelProperty(value = "工厂所在地", index = 16)
    @ColumnWidth(10)
    private String plantAddrNames;

    /**
     * 结算方式
     */
    @ExcelProperty(value = "结算方式", index = 17)
    @ColumnWidth(30)
    private String payMethodName;

    /**
     * 付款条件
     */
    @ExcelProperty(value = "付款条件", index = 18)
    @ColumnWidth(30)
    private String paymentConditionName;

    /**
     * 采购员
     */
    @ExcelProperty(value = "采购员", index = 19)
    @ColumnWidth(10)
    private String purchaseUserName;


    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 20)
    @ColumnWidth(20)
    private String approveUserName;


    /**
     * 审核完成时间
     */
    @ExcelProperty(value = "审核完成时间", index = 21)
    @ColumnWidth(10)
    private LocalDateTime approveTime;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 22)
    @ColumnWidth(20)
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 23)
    @ColumnWidth(10)
    private String createUserName;

    /**
     * 联系人-人员
     */
    @ExcelProperty(value = "联系人-名称", index = 24)
    private String person;
    /**
     * 联系人-职务
     */
    @ExcelProperty(value = "联系人-职务", index = 25)
    private String position;
    /**
     * 联系人-电话
     */
    @ExcelProperty(value = "联系人-电话", index = 26)
    private String telNumber;
    /**
     * 联系人-邮箱
     */
    @ExcelProperty(value = "联系人-邮箱", index = 27)
    private String email;
    /**
     * 联系人-默认联系人，是/否
     */
    @ExcelProperty(value = "联系人-默认联系人", index = 28)
    private String contactIsDefaultName;
    /**
     * 联系人-启用状态，是/否
     */
    @ExcelProperty(value = "联系人-启用状态", index = 29)
    private String contactDisabledName;
    /**
     * 联系人-备注
     */
    @ExcelProperty(value = "联系人-备注", index = 30)
    private String contactRemark;
    /**
     * 账户-账户名称
     */
    @ExcelProperty(value = "账户-账户名称", index = 31)
    private String payee;
    /**
     * 账户-收款银行
     */
    @ExcelProperty(value = "账户-收款银行", index = 32)
    private String bankName;
    /**
     * 账户-开户支行
     */
    @ExcelProperty(value = "账户-开户支行", index = 33)
    private String bankSubbranch;
    /**
     * 账户-银行账号
     */
    @ExcelProperty(value = "账户-银行账号", index = 34)
    private String bankAccount;
    /**
     * 账户-支付方式
     */
    @ExcelProperty(value = "账户-支付方式", index = 35)
    private String bankPayMethodName;
    /**
     * 账户-是否默认,是/否
     */
    @ExcelProperty(value = "账户-是否默认", index = 36)
    private String accountDefaultName;
    /**
     * 账户-备注
     */
    @ExcelProperty(value = "账户-备注", index = 37)
    private String accountRemark;
    /**
     * 资质-名称
     */
    @ExcelProperty(value = "资质-名称", index = 38)
    private String credentialName;
    /**
     * 资质-有效期
     */
    @ExcelProperty(value = "资质-有效期", index = 39)
    private LocalDate effectiveDate;
    /**
     * 资质-失效期
     */
    @ExcelProperty(value = "资质-失效期", index = 40)
    private LocalDate expireDate;
    /**
     * 资质-证件名称
     */
    @ExcelProperty(value = "资质-证件名称", index = 41)
    private String attachmentName;
    /**
     * 资质-备注
     */
    @ExcelProperty(value = "资质-备注", index = 42)
    private String credentialRemark;

}
