package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 采购入库导出DTO
 * @date 2023/4/13 17:30
 */
@Data
@NoArgsConstructor
public class PurchaseStockExportExcelDTO implements Serializable {

    /**
     * 入库单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库单号", index = 0)
    private String code;

    /**
     * 采购单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购单号", index = 1)
    private String purchaseOrderCode;

    /**
     * 供应商
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "供应商", index = 2)
    private String supplierName;

    /**
     * 单据状态
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "单据状态", index = 3)
    private String approveStatusName;

    /**
     * 作废状态
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "作废状态", index = 4)
    private String invalidStatusName;

    /**
     * SKU
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 5)
    private String skuNo;

    /**
     * 产品名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 6)
    private String productName;

    /**
     * 入库日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库日期", index = 7)
    private String stockInDate;

    /**
     * 采购数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购数量", index = 8)
    private Integer purchaseQty;

    /**
     * 收货数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货数量", index = 9)
    private Integer receiveQty;

    /**
     * 入库数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库数量", index = 10)
    private Integer stockInQty;

    /**
     * 超收数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "超收数量", index = 11)
    private Integer exceedQty;

    /**
     * 交货仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "交货仓库", index = 12)
    private String deliveryWarehouseName;

    /**
     * 采购员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购员", index = 13)
    private String purchaseUserName;

    /**
     * 入库员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库员", index = 14)
    private String stockInUserName;

    /**
     * 入库备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库备注", index = 15)
    private String remark;

    /**
     * 审核人（最新）
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "审核人（最新）", index = 16)
    private String approveUserName;

    /**
     * 创建人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建人", index = 17)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建时间", index = 18)
    private String createTime;
}
