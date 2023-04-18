package com.erp.model.wms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
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
public class ReturnOrderExcelDTO {
    /**
     * 退货单号
     */
    private String code;

    /**
     * 采购单号
     */
    private String purchaseOrderCode;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 单据状态
     */
    private String approveStatus;

    /**
     * 审核状态名称
     */
    private String approveStatusName;

    /**
     * 作废状态
     */
    private Boolean invalidStatus;

    /**
     * 作废状态名称
     */
    private String invalidStatusName;

    /**
     * skuId
     */
    private String skuId;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 退货日期
     */
    private LocalDate billDate;

    /**
     * 交货仓库
     */
    private String deliveryWarehouseName;

    /**
     * 退货数量
     */
    private String realityReturnQty;

    /**
     * 退货原因
     */
    private String returnRemark;

    /**
     * 退货方式
     */
    private String returnMode;

    /**
     * 采购员
     */
    private String purchaseUserName;

    /**
     * 退货员
     */
    private String returnUserName;

    /**
     * 退货备注
     */
    private String remark;

    /**
     * 审核人
     */
    private String approveUserName;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private String createTime;

}
