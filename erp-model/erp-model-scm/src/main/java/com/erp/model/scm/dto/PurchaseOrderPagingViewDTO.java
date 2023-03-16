package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 11:15
 */
@Data
@NoArgsConstructor
public class PurchaseOrderPagingViewDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 采购单号
     */
    private String code;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 审核状态
     */
    private String approveStatusName;

    /**
     * 作废状态（0未作废，1已作废）
     */
    private String invalidStatusName;

    /**
     * 到货状态（0未到货，1部分到货，2已到货）
     */
    private String arrivalStatusName;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 预计交货日期
     */
    private Date planDeliveryDate;

    /**
     * 交货仓库名称
     */
    private String deliveryWarehouseName;

    /**
     * 含税单价
     */
    private BigDecimal taxPrice;

    /**
     * 采购数量
     */
    private Integer purchaseQty;

    /**
     * 采购金额
     */
    private BigDecimal purchaseAmount;

    /**
     * 签收数量
     */
    private Integer receiveQty;

    /**
     * 入库数量
     */
    private Integer stockInQty;

    /**
     * 交货数量
     */
    private Integer deliveryQty;

    /**
     * 退货数量
     */
    private Integer returnQty;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审核人
     */
    private String approveUserName;

    /**
     * 申请人
     */
    private String purchaseUserName;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
