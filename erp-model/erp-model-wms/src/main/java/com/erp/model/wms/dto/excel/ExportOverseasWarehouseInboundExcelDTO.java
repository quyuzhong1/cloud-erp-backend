package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.utils.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 海外入库单导出实体
 * @author Jim
 * @date 2023/11/27
 */
@Data
@NoArgsConstructor
public class ExportOverseasWarehouseInboundExcelDTO implements Serializable {

    /**
     * 入库单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库单号", index = 0)
    private String code;

    /**
     * 来源单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "来源单号", index = 1)
    private String sourceCode;

    /**
     * 入库类型
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库类型", index = 2)
    private String instockTypeName;

    /**
     * 交货方式
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "交货方式", index = 3)
    private String deliveryModeName;

    /**
     * 入库状态
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "入库状态", index = 4)
    private String instockStatusName;

    /**
     * 发货仓
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "发货仓", index = 5)
    private String deliveryWarehouseName;

    /**
     * 中转仓
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "中转仓", index = 6)
    private String transferWarehouseName;

    /**
     * 目的仓
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "目的仓", index = 7)
    private String toWarehouseName;

    /**
     * 物流方式
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "物流方式", index = 8)
    private String logisticsMethodName;

    /**
     * 海外仓平台产品名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "海外仓平台产品名称", index = 9)
    private String platformProductName;

    /**
     * 海外仓平台SKU号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "海外仓平台SKU号", index = 10)
    private String platformSkuNo;

    /**
     * ERP系统产品名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "ERP系统产品名称", index = 11)
    private String productName;

    /**
     * ERP的SKU
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "ERP的SKU", index = 12)
    private String skuNo;

    /**
     * 签收数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "签收数量", index = 13)
    private String receiveQty;

    /**
     * 在途数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "在途数量", index = 14)
    private String transportQty;


    /**
     * 装箱数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "装箱数量", index = 15)
    private String packQty;

    /**
     * 收发差异
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收发差异", index = 16)
    private String diffQty;

    /**
     * 签收时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "签收时间", index = 17, converter = LocalDateStringConverter.class)
    private LocalDateTime detailReceiveTime;

    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 18)
    private String remark;

    /**
     * 审核人（最新）
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "审核人（最新）", index = 20)
    private String approveUserName;

    /**
     * 创建人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建人", index = 21)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建时间", index = 22, converter = LocalDateStringConverter.class)
    private LocalDateTime createTime;
}
