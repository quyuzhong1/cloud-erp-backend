package com.erp.model.mrp.vo;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import com.erp.model.mrp.enums.ReplenishmentBillStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OverseasInTransitDetailVO {

    private String id;
    /**
     * 发货单id
     */
    private String deliveryPlanId;

    /**
     * 发货单code
     */
    private String deliveryPlanCode;

    /**
     * 状态
     */
    @Dict(enumClass = ReplenishmentBillStatusEnum.class)
    private String status;

    /**
     * 发货日期
     */
    private LocalDate deliveryDate;

    /**
     * 发货数量
     */
    private Integer deliveryQty;

    /**
     * 签收数量
     */
    private Integer receiveQty;

    /**
     * 在途
     */
    private Integer inTransitQty;

    /**
     * 预计到货日期
     */
    private LocalDate estimateSalesDate;

    /**
     * 店铺在途明细
     */
    private Integer shopInTransitQty;

    /**
     * 仓库id
     */
    @Dict(serviceCode = ServiceCodeNameEnum.WMS, queryFieldName = "id" , tableName = "warehouse")
    private String warehouseId;
}
