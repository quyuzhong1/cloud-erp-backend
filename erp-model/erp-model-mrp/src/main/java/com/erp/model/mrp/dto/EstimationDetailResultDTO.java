package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EstimationDetailResultDTO {

    /**
     * 日期
     */
    private LocalDate date;
    /**
     * 库存
     */
    private BigDecimal inventoryQty;

    /**
     * 试算库存
     */
    private BigDecimal calcInventoryQty;
    /**
     * 当日销量
     */
    private BigDecimal salesQty;

    /**
     * 当日到货数量
     */
    private BigDecimal planArrivalQty;

    /**
     * 试算当日到货数量
     */
    private BigDecimal calcPlanArrivalQty;

    /**
     * FBA在途
     */
    private Integer fbaInTransitQty;

    /**
     * FBA 预计发货数量
     */
    private Integer fbaPlanDeliveryQty;

    /**
     * 海外仓在途
     */
    private Integer overseasInTransitQty;

    /**
     * 海外仓预计发货
     */
    private Integer overseasPlanDeliveryQty;

    /**
     * 本地仓在途
     */
    private Integer localInTransitQty;

    /**
     * 本地仓预计采购
     */
    private Integer localPlanPurchaseQty;

}
