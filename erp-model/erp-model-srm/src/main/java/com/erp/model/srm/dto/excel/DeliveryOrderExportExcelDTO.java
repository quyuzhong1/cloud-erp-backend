package com.erp.model.srm.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.utils.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发货单导出
 */
@Data
public class DeliveryOrderExportExcelDTO implements Serializable {

    @ExcelIgnore
    private String id;

    @ExcelIgnore
    private String detailId;
    @ExcelIgnore
    private String purchaseDetailId;
    /**
     * 送货单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "送货单号", index = 0)
    private String code;

    /**
     * 预计到达日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "预计到达日期", index = 1)
    private LocalDate planDeliveryDate;


    /**
     * 收货单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货单号", index = 2)
    private String receiveCode;

    /**
     * 是否打印
     */
    @ExcelIgnore
    private Boolean isPrint;

    /**
     * 打印状态
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "打印状态", index = 3)
    private String printStatus;

    /**
     * 收货状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收货状态", index = 4)
    private String receiptStatus;

    /**
     * 供应商Id
     */
    @ExcelIgnore
    private String supplierId;

    /**
     * 供应商Id
     */
    @ExcelIgnore
    private String sourceId;

    /**
     * 供应商名称
     */
    @ColumnWidth(40)
    @ExcelProperty(value = "供应商名称", index = 5)
    private String supplierName;

    /**
     * 采购单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购单号", index = 6)
    private String sourceCode;

    /**
     * skuNo
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 7)
    private String skuNo;

    /**
     * 产品名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "产品名称", index = 8)
    private String productName;

    /**
     * 订单数量
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "订单数量", index = 9)
    private Integer orderQty;

    /**
     * 送货数量
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "送货数量", index = 10)
    private Integer deliveryQty;

    /**
     * 赠品数量（送货）
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "赠品数量（送货）", index = 11)
    private Integer giftQty;

    /**
     * 收货数量
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "收货数量", index = 12)
    private Integer receiveQty;

    /**
     * 赠品收货数量
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "赠品数量（收货）", index = 13)
    private Integer giftReceiveQty;

    /**
     * 质检合格数
     */
    @ExcelProperty(value = "质检合格数", index = 14)
    @ColumnWidth(10)
    private Integer qcGoodQty;

    /**
     * 采购组织
     */
    @ExcelProperty(value = "采购组织", index = 15)
    @ColumnWidth(40)
    private String customerName;

    /**
     * 目的仓
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓库", index = 16)
    private String toWarehouseName;

    /**
     * 打印日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "送货打印日期", index = 17)
    private LocalDate printDate;

    /**
     * 确认收货日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "确认收货日期", index = 18)
    private LocalDate confirmReceiveDate;

    /**
     * 收货员名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收货员名", index = 19)
    private String receiveUserName;

    /**
     * 创建人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建人", index = 20)
    private String createUserName;


    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 21,converter = LocalDateStringConverter.class)
    private LocalDateTime createTime;
}
