package com.erp.model.wms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
     * 供应商
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
     * 退货仓库
     */
    private String returnWarehouseName;

    /**
     * 退货数量
     */
    private String returnQty;

    /**
     * 退货原因
     */
    private String returnRemark;

    /**
     * 退货方式
     */
    private String returnMode;

    /**
     * 退货方式名称
     */
    private String returnModeName;

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
    private LocalDateTime createTime;

}
