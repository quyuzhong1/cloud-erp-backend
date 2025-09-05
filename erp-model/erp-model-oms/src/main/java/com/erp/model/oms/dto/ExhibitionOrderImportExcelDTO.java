package com.erp.model.oms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author jack
 * @Date 2025-08-26
 */
@Data
@NoArgsConstructor
public class ExhibitionOrderImportExcelDTO implements Serializable {


    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号",isNotBlank = true )
    private String no;


    /**
     * 单据日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*单据日期", index = 1)
    @FieldValid(fieldName = "*单据日期",isNotBlank = true)
    private String billDateStr;
    @ExcelIgnore
    private LocalDate billDate;
    /**
     * 销售组织
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*销售组织", index = 2)
    @FieldValid(fieldName = "*销售组织",isNotBlank = true)
    private String salesOrgName;
    @ExcelIgnore
    private String salesOrgId;

    /**
     * 领用人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*领用人", index = 3)
    @FieldValid(fieldName = "*领用人",isNotBlank = true)
    private String recipientUserName;
    /**
     * 销售员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*销售员", index = 4)
    @FieldValid(fieldName = "*销售员",isNotBlank = true)
    private String sellerName;
    @ExcelIgnore
    private String sellerId;


    /**
     * 仓库
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*仓库", index = 5)
    @FieldValid(fieldName = "*仓库",isNotBlank = true)
    private String warehouseName;
    @ExcelIgnore
    private String warehouseId;

    /**
     * 收款日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*收款日期", index = 6)
    @FieldValid(fieldName = "*收款日期",isNotBlank = true)
    private String receiveDate;

    /**
     * 收款方式
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收款方式", index = 7)
    @FieldValid(fieldName = "*收款方式",isNotBlank = true)
    private String receiveMethod;

    /**
     * 收款账号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收款账号", index = 8)
    @FieldValid(fieldName = "*收款账号",isNotBlank = true)
    private String receiveAccount;

    /**
     * 收款金额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收款金额", index = 9)
    @FieldValid(fieldName = "*收款金额",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String receiveAmount;


    /**
     * 是否收取运费
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "是否收取运费", index = 10)
    @FieldValid(fieldName = "是否收取运费")
    private String isCollectShippingFee;

    /**
     * 运费金额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "运费金额", index = 11)
    @FieldValid(fieldName = "运费金额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String shippingFee;

    /**
     * 展会名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "展会名称", index = 12)
    @FieldValid(fieldName = "展会名称")
    private String exhibitionTitle;
    /**
     * 贸易条款
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "贸易条款", index = 13)
    @FieldValid(fieldName = "贸易条款")
    private String tradeTerm;

    /**
     * 折扣总额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "折扣总额", index = 14)
    @FieldValid(fieldName = "折扣总额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String discountAmount;

    /**
     * 客户
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*客户", index = 15)
    @FieldValid(fieldName = "*客户",isNotBlank = true)
    private String customerName;

    /**
     * 收货人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收货人", index = 16)
    @FieldValid(fieldName = "收货人")
    private String receiverName;

    /**
     * 联系电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系电话", index = 17)
    @FieldValid(fieldName = "联系电话")
    private String telNumber;

    /**
     * 收货地址
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收货地址", index = 18)
    @FieldValid(fieldName = "*收货地址",isNotBlank = true)
    private String receiveAddress;

    /**
     * 交货方式
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*交货方式", index = 19)
    @FieldValid(fieldName = "*交货方式",isNotBlank = true)
    private String deliveryMode;
    /**
     * 结算币别
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*结算币别", index = 20)
    @FieldValid(fieldName = "*结算币别",isNotBlank = true)
    private String currency;

    /**
     * 是否含税
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*是否含税", index = 21)
    @FieldValid(fieldName = "*是否含税",isNotBlank = true)
    private String isTax;

    /**
     * 地址类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*地址类型", index = 22)
    @FieldValid(fieldName = "*地址类型",isNotBlank = true)
    private String addressType;

    /**
     * 收款条件
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收款条件", index = 23)
    @FieldValid(fieldName = "*收款条件",isNotBlank = true)
    private String receiveCondition;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 24)
    @FieldValid(fieldName = "备注",maxLength =200)
    private String remark;


    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 25)
    @FieldValid(fieldName = "*SKU",isNotBlank = true)
    private String skuNo;

    /**
     * 使用方
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*使用方", index = 26)
    @FieldValid(fieldName = "*使用方",isNotBlank = true)
    private String useUserName;


    /**
     * 销售数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*销售数量", index = 27)
    @FieldValid(fieldName = "*销售数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String qty;

    /**
     * 销售单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售单价", index = 28)
    @FieldValid(fieldName = "销售单价",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String price;
    /**
     * 税率(%)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率(%)", index = 29)
    @FieldValid(fieldName = "税率(%)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private String taxRate;

    /**
     *  含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "含税单价", index = 30)
    @FieldValid(fieldName = "含税单价",formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String taxPrice;
    /**
     * 是否赠品
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*是否赠品", index = 31)
    @FieldValid(fieldName = "*是否赠品",isNotBlank = true)
    private String isGift;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 32)
    @FieldValid(fieldName = "明细备注",maxLength =200)
    private String detailRemark;

    @ExcelIgnore
    private String sampleLedgerId;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =33)
    @ColumnWidth(50)
    private String  errorMsg;
}
