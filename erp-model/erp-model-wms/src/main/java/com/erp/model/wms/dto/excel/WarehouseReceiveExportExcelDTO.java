package com.erp.model.wms.dto.excel;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签收单导出
 * @Author Luo_WG
 * @Date 2023/4/14 16:01
 **/
@Data
@NoArgsConstructor
public class WarehouseReceiveExportExcelDTO {
    /**
     * 收货单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货单号", index = 0)
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
     * 收货数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货数量", index = 7)
    private Integer receiveQty;

    /**
     * 采购数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购数量", index = 8)
    private Integer purchaseQty;

    /**
     * 超收数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "超收数量", index = 9)
    private Integer exceedQty;

    /**
     * 交货仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "交货仓库", index = 10)
    private String deliveryWarehouseName;


    /**
     * 收货日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货日期", index = 11)
    private LocalDate billDate;

    /**
     * 收货员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货员", index = 12)
    private String receiveUserName;

    /**
     * 采购员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购员", index = 13)
    private String purchaseUserName;

    /**
     * 审核人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "审核人", index = 14)
    private String approveUserName;

    /**
     * 收货备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货备注", index = 15)
    private String remark;

    /**
     * 创建人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建人", index = 16)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建时间", index = 17)
    private LocalDateTime createTime;

}
