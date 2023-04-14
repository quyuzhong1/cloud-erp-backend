package com.erp.model.wms.dto.excel;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 签收单导出
 * @Author Luo_WG
 * @Date 2023/4/14 16:01
 **/
@Data
@NoArgsConstructor
public class WarehouseReceiveExportExcelDTO {
    /**
     * 签收单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "签收单号", index = 0)
    private String code;

    /**
     * 采购单id
     */
    private String purchaseOrderId;

    /**
     * 采购单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购单号", index = 1)
    private String purchaseOrderCode;

    /**
     * 供应商名
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "供应商名", index = 2)
    private String supplierName;

    /**
     * 单据状态
     */
    private String approveStatus;

    /**
     * 审核状态名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "审核状态名称", index = 3)
    private String approveStatusName;

    /**
     * 作废状态
     */
    private Boolean invalidStatus;

    /**
     * 作废状态名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "作废状态名称", index = 4)
    private String invalidStatusName;

    /**
     * skuId
     */
    private String skuId;

    /**
     * skuNo
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "skuNo", index = 5)
    private String skuNo;

    /**
     * 产品名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "产品名称", index = 6)
    private String productName;

    /**
     * 收货日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货日期", index = 7)
    private LocalDate billDate;

    /**
     * 签收数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "签收数量", index = 8)
    private Integer receiveQty;

    /**
     * 交货仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "交货仓库", index = 9)
    private String deliveryWarehouseName;

    /**
     * 收货人名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货人名称", index = 10)
    private String receiveUserName;

    /**
     * 采购员名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购员名称", index = 11)
    private String purchaseUserName;

    /**
     * 采购数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购数量", index = 12)
    private Integer purchaseQty;

    /**
     * 超收数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "超收数量", index = 13)
    private Integer exceedQty;

    /**
     * 审核人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "审核人", index = 14)
    private String approveUserName;

    /**
     * 创建人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建人", index = 15)
    private String createUserName;

    /**
     * 采购单详情表id
     */
    private String purchaseOrderDetailId;
}
