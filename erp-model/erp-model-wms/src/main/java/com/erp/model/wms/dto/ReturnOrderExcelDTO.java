package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private Integer returnQty;

    /**
     * 退款单价
     */
    private BigDecimal returnPrice;

    /**
     * 扣款数量
     */
    private Integer deductAmountQty;

    /**
     * 退款金额
     */
    private BigDecimal deductAmountAmount;

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
     * 审核完成时间
     */
    private LocalDateTime approveTime;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 采购订单详情表id
     */
    private String purchaseOrderDetailId;

    /**
     * 退货确认
     */
    private String confirmStatus;

    /**
     * 退货确认中文
     */
    private String confirmStatusName;

    /**
     * 异常分类
     */
    private String unusualType;

    /**
     * 异常分类中文
     */
    private String unusualTypeName;

    /**
     * 异常反馈描述
     */
    private String unusualRemark;

    /**
     * 异常处理人名称
     */
    private String unusualHandleUserName;

    /**
     * 退货确认日期
     */
    private LocalDate confirmDate;
}
