package com.erp.model.wms.dto.excel;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.service.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 导出退货单
 * @Author Luo_WG
 * @Date 2023/4/14 16:01
 **/
@Data
@NoArgsConstructor
public class ReturnOrderExportExcelDTO {
    /**
     * 退货单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货单号", index = 0)
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
     * 退货日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货数量", index = 7)
    private LocalDate billDate;

    /**
     * 仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库", index = 8)
    private String returnWarehouseName;

    /**
     * 退货数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货数量", index = 9)
    private Integer returnQty;

    /**
     * 退货原因
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货原因", index = 10)
    private String returnRemark;

    /**
     * 退货方式
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货方式", index = 11)
    private String returnModeName;

    /**
     * 退货员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货员", index = 12)
    private String returnUserName;

    /**
     * 退货备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货备注", index = 13)
    private String remark;

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
     * 创建时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建时间", index = 16, converter= LocalDateStringConverter.class)
    private LocalDateTime createTime;
}
