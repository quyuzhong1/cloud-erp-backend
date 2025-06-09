package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单DTO 发货单列表
 *
 * @Author zdy
 **/
@Data
@NoArgsConstructor
public class PlatformDeliveryDTO {

    /**
     * erp 主记录id
     */
    private String mainId;
    /**
     * 来源单号（履约单号）
     */
    private String sourceCode;
    /**
     * 物流单号
     */
    private String trackNo;
    /**
     * 运单号
     */
    private String transportNo;

    /**
     * 订单状态
     */
    private String orderStatus;
    /**
     * 下发到仓时间戳
     */
    private LocalDateTime deliveryWarehouseTime;
    /**
     * 平台仓库名称
     */
    private String platformWarehouseName;

    /**
     * ERP仓库组织id
     */
    private String warehouseOrgId;
    /**
     * ERP仓库组织名称
     */
    private String warehouseOrgName;
    /**
     *来源平台
     */
    private String sourcePlatform = "thirdPlatform";
    /**
     * (速卖通)买家视角订单金额
     */
    private BigDecimal actualAmount = BigDecimal.ZERO;

    /**
     * (速卖通)买家视角订单金额币种
     */
    private String actualCurrency = "";

    /**
     * (速卖通)订单明细结算币种总价
     */
    private BigDecimal orderAmount = BigDecimal.ZERO;

    /**
     * (速卖通)订单明细结算币种
     */
    private String orderCurrency = "";

    /**
     * (速卖通)订单整单税后金额
     */
    private BigDecimal orderAfterTaxAmount = BigDecimal.ZERO;

    /**
     * 发货单明细
     */
    private List<PlatformDeliveryDetailDTO> detailDTOList;

}
