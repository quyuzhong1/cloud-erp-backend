package com.erp.model.wms.dto;


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
public class WarehouseReceiveExcelDTO {
    /**
     * 签收单号
     */
    private String code;

    /**
     * 采购单id
     */
    private String purchaseOrderId;

    /**
     * 采购单号
     */
    private String purchaseOrderCode;

    /**
     * 供应商名
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
     * 收货日期
     */
    private LocalDate billDate;

    /**
     * 签收数量
     */
    private Integer receiveQty;

    /**
     * 交货仓库
     */
    private String deliveryWarehouseName;

    /**
     * 收货人名称
     */
    private String receiveUserName;

    /**
     * 采购员名称
     */
    private String purchaseUserName;

    /**
     * 采购数量
     */
    private Integer purchaseQty;

    /**
     * 超收数量
     */
    private Integer exceedQty;

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
    private LocalDateTime createTime;

    /**
     * 收货备注
     */
    private String remark;

    /**
     * 采购单详情表id
     */
    private String purchaseOrderDetailId;
}
