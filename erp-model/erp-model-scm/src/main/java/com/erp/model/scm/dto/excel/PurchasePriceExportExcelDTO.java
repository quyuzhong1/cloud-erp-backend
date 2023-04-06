package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.service.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname PurchasePriceExportExcelDTO
 * @Description TODO
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
     * sku
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 2)
    private String skuNo;


    /**
     * sku
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "产品名称", index = 3)
    private String productName;

    /**
     * 区间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区间从-到", index = 4)
    private String qtySection;





    /**
     * 含税单价
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "含税单价", index = 5)
    private String taxPrice;


    /**
     * 含税单价
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "税率(%)", index = 6)
    private BigDecimal taxRate;


    /**
     * 生效时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "生效时间", index = 7)
    private LocalDate effectiveDate;

    /**
     * 采购组织名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "采购组织", index = 8)
    private String purchaseOrgName;

    /**
     * 单据状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "单据状态", index = 9)
    private String approveStatusName;


    @ColumnWidth(10)
    @ExcelProperty(value = "创建人", index = 10)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 11,converter= LocalDateStringConverter.class)
    private LocalDateTime createTime;


}
