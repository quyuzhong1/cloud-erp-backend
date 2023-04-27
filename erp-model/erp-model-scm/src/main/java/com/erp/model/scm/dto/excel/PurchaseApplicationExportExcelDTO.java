package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.service.LocalDateStringConverter;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: 采购申请单导出DTO
 * @date 2023/3/22 12:18
 */
@Data
public class PurchaseApplicationExportExcelDTO implements Serializable {

    /**
     * 申请单号
     */
    @ExcelProperty(value = "申请单号", index = 0)
    private String  code;

    /**
     * 单据状态
     */
    @ExcelProperty(value = "单据状态", index = 1)
    private String  approveStatusName;

    /**
     * 新品首批
     */
    @ExcelProperty(value = "新品首批", index = 2)
    private String  isFirstMassProduct;

    /**
     * 采购单关联状态
     */
    @ExcelProperty(value = "采购单关联状态", index = 3)
    private String  createPoTypeName;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 4)
    private String  skuNo;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 5)
    private String  productName;

    /**
     * 计划交期
     */
    @ExcelProperty(value = "计划交期", index = 6)
    private String planDeliveryDate;

    /**
     * 申请数量
     */
    @ExcelProperty(value = "申请数量", index = 7)
    private String applyQty;

    /**
     * 实际采购数量
     */
    @ExcelProperty(value = "实际采购数量", index = 8)
    private String realPurchaseQty;

    /**
     * 签收数量
     */
    @ExcelProperty(value = "签收数量", index = 9)
    private String receiveQty;

    /**
     * 入库数量
     */
    @ExcelProperty(value = "入库数量", index = 10)
    private String stockInQty;

    /**
     * 目的仓库名称
     */
    @ExcelProperty(value = "目的仓库名称", index = 11)
    private String destWarehouseName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 12)
    private String remark;

    /**
     * 审核人
     */
    @ExcelProperty(value = "审核人", index = 13)
    private String approveUserName;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 14)
    private String createUserName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 15 ,converter= LocalDateStringConverter.class)
    private LocalDateTime createTime;
}
