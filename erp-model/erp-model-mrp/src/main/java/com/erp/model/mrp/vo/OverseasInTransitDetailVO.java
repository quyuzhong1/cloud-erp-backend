package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OverseasInTransitDetailVO {


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
    private String status;
    /**
     * 状态名字
     */
    private String statusName;

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
    private LocalDate planArrivalDate;
}
