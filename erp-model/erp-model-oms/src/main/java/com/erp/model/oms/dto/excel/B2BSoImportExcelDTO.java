package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 *
 * @author Lambda
 */
@Data
@NoArgsConstructor
public class B2BSoImportExcelDTO {



    @ColumnWidth(10)
    @ExcelProperty(value = "序号", index = 0)
    @FieldValid(fieldName = "序号", isNotBlank = true )
    private String no;


    /**
     * 单据日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "单据类型", index = 1)
    @FieldValid(fieldName = "单据类型", isNotBlank = true)
    private String orderTypeStr;


    /**
     * 单据日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "单据日期", index = 2)
    @FieldValid(fieldName = "单据日期", isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.DATE)
    private String billDate;



    /**
     * 销售组织
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "销售组织", index = 3)
    @FieldValid(fieldName = "销售组织", isNotBlank = true)
    private String salesOrgName;





    /**
     * 销售部门
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售部门", index = 4)
    @FieldValid(fieldName = "销售部门",isNotBlank = true)
    private String salesDeptName;

    /**
     * 销售员
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售员", index = 5)
    @FieldValid(fieldName = "销售员",isNotBlank = true)
    private String sellerName;
    /**
     * 单据子类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*单据子类型", index = 6)
    @FieldValid(fieldName = "单据子类型",isNotBlank = true)
    private String transactionSubTypeName;

    /**
     * 是否收取运费
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "是否收取运费", index = 7)
    @FieldValid(fieldName = "是否收取运费",fieldValues = "是,否")
    private String isCollectShippingFee;

    /**
     * 仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库", index = 8)
    @FieldValid(fieldName = "仓库",isNotBlank = true)
    private String warehouseName;


    /**
     * 银行手续费
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "银行手续费", index = 9)
    @FieldValid(fieldName = "银行手续费",formatPattern=FieldFormatPatternTypeEnum.AMOUNT)
    private String bankServiceFee;


    /**
     * 运费
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "运费", index = 10)
    @FieldValid(fieldName = "运费",formatPattern=FieldFormatPatternTypeEnum.AMOUNT)
    private String shippingFee;

    /**
     * 收款账号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收款账号", index = 11)
    @FieldValid(fieldName = "收款账号",isNotBlank = true)
    private String receiveAccount;

    /**
     * 收款方式
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收款方式", index = 12)
    @FieldValid(fieldName = "收款方式",isNotBlank = true)
    private String receiveMethod;


    /**
     * 收款日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收款日期", index = 13)
    @FieldValid(formatPattern= FieldFormatPatternTypeEnum.DATE)
    private String receiveDate;


    /**
     * 收款金额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收款金额", index = 14)
    @FieldValid(fieldName = "收款金额",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String receiveAmount;


    /**
     * 贸易条款
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "贸易条款", index = 15)
    @FieldValid(fieldName = "贸易条款")
    private String tradeTerm;


    /**
     * 报关费
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "报关费", index = 16)
    @FieldValid(fieldName = "报关费",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String customsFee;


    /**
     * 要货日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "要货日期", index = 17)
    @FieldValid(fieldName = "要货日期",isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.DATE)
    private String requireDate;


    /**
     * 折扣总额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "折扣总额", index = 18)
    @FieldValid(fieldName = "折扣总额",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String discountAmount;


    /**
     * 客户
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "客户", index = 19)
    @FieldValid(fieldName = "客户",isNotBlank = true)
    private String customerName;


    /**
     * 收货人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收货人", index = 20)
    @FieldValid(fieldName = "收货人")
    private String receiverName;

    /**
     * 联系电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系电话", index = 21)
    @FieldValid(fieldName = "联系电话",maxLength = 50)
    private String telNumber;

    /**
     * 收货地址
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收货地址", index = 22)
    @FieldValid(fieldName = "收货地址",isNotBlank = true)
    private String receiveAddress;

    /**
     * 交货方式
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "交货方式", index = 23)
    @FieldValid(fieldName = "交货方式",isNotBlank = true)
    private String deliveryMode;

    /**
     * 结算币别
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "结算币别", index = 24)
    @FieldValid(fieldName = "结算币别",isNotBlank = true)
    private String currency;

    /**
     * 是否含税
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否含税", index = 25)
    @FieldValid(fieldName = "是否含税",isNotBlank = true,fieldValues = "是,否")
    private String isTax;

    /**
     * 地址类型
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "地址类型", index = 26)
    @FieldValid(fieldName = "地址类型",isNotBlank = true)
    private String addressType;

    /**
     * 收款条件
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收款条件", index = 27)
    @FieldValid(fieldName = "收款条件",isNotBlank = true)
    private String receiveCondition;


    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 28)
    @FieldValid(fieldName = "备注")
    private String remark;

    /**
     * SKU
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 29)
    @FieldValid(fieldName = "SKU")
    private String skuNo;
    /**
     * 客户SKU
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "客户SKU", index = 30)
    @FieldValid(fieldName = "客户SKU")
    private String customerSku;
    /**
     * 销售数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售数量", index = 31)
    @FieldValid(fieldName = "销售数量", isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String qty;


    /**
     * 销售单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售单价", index = 32)
    @FieldValid(fieldName = "销售单价", formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String  price;


    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率", index = 33)
    @FieldValid(fieldName = "税率",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String taxRate;
    /**
     * 含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "含税单价", index = 34)
    @FieldValid(fieldName = "含税单价",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String taxPrice;

    /**
     * 是否赠品
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否赠品", index = 35)
    @FieldValid(fieldName = "是否赠品",isNotBlank = true,fieldValues = "是,否")
    private String isGift;

    /**
     * 是否补发
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否补发", index = 36)
    @FieldValid(fieldName = "是否补发",isNotBlank = true,fieldValues = "是,否")
    private String isReissue;


//    /**
//     * 是否关闭
//     */
//    @ColumnWidth(10)
//    @ExcelProperty(value = "是否关闭", index = 37)
//    @FieldValid(fieldName = "是否关闭",isNotBlank = true,fieldValues = "是,否")
//    private String isClose;
    /**
     * 客户PO号
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "客户PO号", index = 37)
    @FieldValid(fieldName = "客户PO号",maxLength = 30)
    private String customerPO;
    /**
     * 目的地
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "目的地", index = 38)
    @FieldValid(fieldName = "目的地",maxLength = 100)
    private String toCountry;

    /**
     * 备注
     */
    @ColumnWidth(40)
    @ExcelProperty(value = "备注", index = 39)
    @FieldValid(fieldName = "备注",maxLength=200)
    private String detailRemark;



    /**
     * 错误信息
     */
    @ColumnWidth(100)
    @ExcelProperty(value = "错误数据", index = 40)
    private String errorMsg;














}
