package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/31 15:54
 */
@Data
public class PurchaseChangeExportExcelDTO implements Serializable {

    /**
     * 变更单号
     */
    @ExcelProperty(value = "变更单号", index = 0)
    private String code;

    /**
     * 采购单号
     */
    @ExcelProperty(value = "采购单号", index = 1)
    private String poCode;

    /**
     * 供应商
     */
    @ExcelProperty(value = "供应商", index = 2)
    private String supplierName;

    /**
     * 单据状态
     */
    @ExcelProperty(value = "单据状态", index = 3)
    private String approveStatusName;

    /**
     * 作废状态
     */
    @ExcelProperty(value = "作废状态", index = 4)
    private String invalidStatusName;

    /**
     * sku编码
     */
    @ExcelProperty(value = "SKU", index = 5)
    private String skuNo;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 6)
    private String productName;

    /**
     * 原采购重量
     */
    @ExcelProperty(value = "原采购量", index = 7)
    private Integer oldQty;


    /**
     * 交货仓库名称
     */
    @ExcelProperty(value = "交货仓库", index = 8)
    private String deliveryWarehouseName;

    /**
     * 原币别
     */
    @ExcelProperty(value = "原币别", index = 9)
    private String currency;

    /**
     * 原采购单价
     */
    @ExcelProperty(value = "原采购单价", index = 10)
    private BigDecimal oldPrice;

    /**
     * 原采购总额
     */
    @ExcelProperty(value = "原采购总额", index = 11)
    private BigDecimal oldAmount;

    /**
     * 新采购量
     */
    @ExcelProperty(value = "新采购量", index = 12)
    private Integer qty;

    /**
     * 新采购单价
     */
    @ExcelProperty(value = "新采购单价", index = 13)
    private BigDecimal price;

    /**
     * 新采购总额
     */
    @ExcelProperty(value = "新采购总额", index = 14)
    private BigDecimal amount;

    /**
     * 变更原因
     */
    @ExcelProperty(value = "变更原因", index = 15)
    private String remark;

    /**
     * 申请人
     */
    @ExcelProperty(value = "申请人", index = 16)
    private BigDecimal changeUserName;

    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 17)
    private BigDecimal approveUserName;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 18)
    private String createUserName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 19)
    private String createTime;
}
