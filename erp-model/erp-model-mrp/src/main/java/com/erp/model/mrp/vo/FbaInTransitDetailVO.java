package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FbaInTransitDetailVO {


    /**
     * 来源id
     */
    private String sourceId;

    /**
     * 来源单号
     */
    private String sourceCode;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 发货状态
     */
    private String status;

    /**
     * 发货状态名字
     */
    private String statusName;

    /**
     * 发货日期
     */
    private LocalDate deliveryDate;

    /**
     * 申报数量
     */
    private Integer declareQty;

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
}
