package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/27 15:57
 */
@Data
public class PurchaseOrderExportExcelDTO implements Serializable {


    /**
     * 采购单号
     */
    @ExcelProperty(value = "采购单号", index = 0)
    private String code;

    /**
     * 供应商
     */
    @ExcelProperty(value = "供应商", index = 1)
    private String supplierName;

    /**
     * 审核状态名称（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
     */
    @ExcelProperty(value = "单据状态", index = 2)
    private String approveStatusName;

    /**
     * 作废状态（0未作废，1已作废）
     */
    @ExcelProperty(value = "作废状态", index = 3)
    private String invalidStatusName;

    /**
     * 到货状态（0未到货，1部分到货，2已到货）
     */
    @ExcelProperty(value = "到货状态", index = 4)
    private String arrivalStatusName;

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
     * 报关名称
     */
    @ExcelProperty(value = "报关名称", index = 7)
    private String declareName;

    /**
     * 报关型号
     */
    @ExcelProperty(value = "报关型号", index = 8)
    private String declareModel;

    /**
     * 计划交期
     */
    @ExcelProperty(value = "计划交期", index = 9)
    private String planDeliveryDate;

    /**
     * 交货仓库名称
     */
    @ExcelProperty(value = "交货仓库", index = 10)
    private String deliveryWarehouseName;

    /**
     * 含税单价
     */
    @ExcelProperty(value = "含税单价", index = 11)
    private BigDecimal taxPrice;

    /**
     * 采购数量
     */
    @ExcelProperty(value = "采购数量", index = 12)
    private Integer purchaseQty;

    /**
     * 采购金额
     */
    @ExcelProperty(value = "采购金额", index = 13)
    private BigDecimal purchaseAmount;

    /**
     * 待交货量
     */
    @ExcelProperty(value = "待交货量", index = 14)
    private Integer deliveryQty;

    /**
     * 签收数量
     */
    @ExcelProperty(value = "签收数量", index = 15)
    private Integer receiveQty;

    /**
     * 入库数量
     */
    @ExcelProperty(value = "入库数量", index = 16)
    private Integer stockInQty;

    /**
     * 退货数量
     */
    @ExcelProperty(value = "退货数量", index = 17)
    private Integer returnQty;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 18)
    private String remark;

    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 19)
    private String approveUserName;

    /**
     * 申请人
     */
    @ExcelProperty(value = "申请人", index = 20)
    private String purchaseUserName;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 21)
    private String createUserName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 22)
    private LocalDateTime createTime;

}
