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
 * 采购变更导出
 * @author Will
 * @date: 2023/10/18 16:52
 */
@Data
@NoArgsConstructor
public class PurchasePriceChangeExportExcelDTO implements Serializable {

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
     * 单据状态名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "单据状态", index = 2)
    private String approveStatusName;

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
     * 调整前含税单价
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "调整前含税单价", index = 6)
    private BigDecimal oldTaxPrice;

    /**
     * 调整后含税单价
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "调整后含税单价", index = 7)
    private BigDecimal taxPrice;

    /**
     * 调整前税率
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "调价前税率(%)", index = 8)
    private BigDecimal oldTaxRate;

    /**
     * 调整后税率
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "调整后税率(%)", index = 9)
    private BigDecimal taxRate;

    /**
     * 采购组织名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "采购组织", index = 10)
    private String purchaseOrgName;

    /**
     * 生效时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "生效时间", index = 11)
    private LocalDate effectiveDate;

    /**
     * 升降比例
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "升降比例", index = 12)
    private String offsetRate;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 13)
    private String detailRemark;

    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 14)
    @ColumnWidth(20)
    private String approveUserName;

    /**
     * 审核完成时间
     */
    @ExcelProperty(value = "审核完成时间", index = 15,converter= LocalDateStringConverter.class)
    @ColumnWidth(20)
    private LocalDateTime approveTime;

    /**
     * 创建人
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "创建人", index = 16)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 17,converter= LocalDateStringConverter.class)
    private LocalDateTime createTime;


}
