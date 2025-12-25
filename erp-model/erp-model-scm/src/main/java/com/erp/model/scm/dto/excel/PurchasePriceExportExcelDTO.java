package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.utils.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname PurchasePriceExportExcelDTO

 * @Date 2023-03-27 18:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceExportExcelDTO implements Serializable {

    /**
     * 单据编号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "单据编号", index = 0)
    private String code;



    /**
     * 供应商名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "供应商名称", index = 1)
    private String supplierName;


    /**
     * 是否含税
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "是否含税", index = 2)
    private String isTaxIncludedName;



    /**
     * sku
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 3)
    private String skuNo;


    /**
     * sku
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "产品名称", index = 4)
    private String productName;

    /**
     * 区间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区间从-到", index = 5)
    private String qtySection;





    /**
     * 含税单价
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "含税单价", index = 6)
    private String taxPrice;


    /**
     * 含税单价
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "税率(%)", index = 7)
    private BigDecimal taxRate;


    /**
     * 生效时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "生效时间", index = 8)
    private LocalDate effectiveDate;


    /**
     * 失效时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "失效时间", index = 9)
    private LocalDate expireDate;

    /**
     * 生效时间
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "启用状态", index = 10)
    private String enabled;

    /**
     * 采购组织名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "采购组织", index = 11)
    private String purchaseOrgName;

    /**
     * 单据状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "单据状态", index = 12)
    private String approveStatusName;


    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 13)
    @ColumnWidth(20)
    private String approveUserName;

    /**
     * 审核完成时间
     */
    @ExcelProperty(value = "审核完成时间", index = 14,converter= LocalDateStringConverter.class)
    @ColumnWidth(20)
    private LocalDateTime approveTime;


    @ColumnWidth(10)
    @ExcelProperty(value = "创建人", index = 15)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 16,converter= LocalDateStringConverter.class)
    private LocalDateTime createTime;


    private String voucherNo;
}
