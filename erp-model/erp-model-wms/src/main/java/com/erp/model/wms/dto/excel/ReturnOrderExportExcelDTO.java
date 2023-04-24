package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.service.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 退货单
 * @Author Luo_WG
 * @Date 2023/4/17 18:49
 **/
@Data
@NoArgsConstructor
public class ReturnOrderExportExcelDTO {
    /**
     * 退货单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "签收单号", index = 0)
    private String code;

    /**
     * 采购单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购单号", index = 1)
    private String purchaseOrderCode;

    /**
     * 供应商名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "供应商名称", index = 2)
    private String supplierName;

    /**
     * 审核状态名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "审核状态名称", index = 3)
    private String approveStatusName;

    /**
     * 作废状态名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "作废状态名称", index = 4)
    private String invalidStatusName;

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
    @ExcelProperty(value = "productName", index = 6)
    private String productName;

    /**
     * 退货日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货日期", index = 7)
    private LocalDate billDate;

    /**
     * 交货仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "交货仓库", index = 8)
    private String deliveryWarehouseName;

    /**
     * 退货数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货数量", index = 9)
    private String realityReturnQty;

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
    private String returnMode;

    /**
     * 采购员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购员", index = 12)
    private String purchaseUserName;

    /**
     * 退货员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货员", index = 13)
    private String returnUserName;

    /**
     * 退货备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "退货备注", index = 14)
    private String remark;

    /**
     * 审核人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "审核人", index = 15)
    private String approveUserName;

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
    @ExcelProperty(value = "创建时间", index = 17, converter= LocalDateStringConverter.class)
    private String createTime;

}
