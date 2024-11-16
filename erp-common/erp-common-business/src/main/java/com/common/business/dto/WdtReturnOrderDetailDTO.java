package com.common.business.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WdtReturnOrderDetailDTO implements Serializable {

    /**
     * sku编号
     */
    private String skuNo;

    /**
     * 应退数量
     */
    private Integer mustQty;

    /**
     * 签收数量
     */
    private Integer receiveQty;

    /**
     * 实退数量
     */
    private Integer realQty;

    /**
     * 退货类型：dict_basic表type = returnType  退货退款  退货补货
     */
    private String returnTypeDict;

    /**
     * 退货原因
     */
    private String returnReasonDict;

    /**
     * 仓位
     */
    private String warehouseLocation;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源明细id
     */
    private String sourceDetailId;

    /**
     * 退货单详情
     */
    private String soReturnDetailId;

    /**
     * 仓库id
     */
    private String warehouseId;

    /**
     * 是否委外（true是、false否）
     */
    private Boolean isSubContract;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 审核状态
     */
    private String approveStatus;
    
    /**
     * 退款金额
     */
    private BigDecimal amount;
}
