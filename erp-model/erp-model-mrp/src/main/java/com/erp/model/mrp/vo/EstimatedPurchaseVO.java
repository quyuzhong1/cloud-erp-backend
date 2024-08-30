package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EstimatedPurchaseVO {

    /**
     * 状态
     */
    private String status;
    /**
     * 状态名字
     */
    private String statusName;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 预计到货日期
     */
    private LocalDate planArrivalDate;

    /**
     * 业务类型 本地
     */
    private String type;

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
}
