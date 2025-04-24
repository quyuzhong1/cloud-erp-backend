package com.erp.model.mrp.vo;

import com.common.business.annotation.Dict;
import com.erp.model.mrp.enums.ReplenishmentBillStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EstimatedPurchaseVO {

    private String id;

    /**
     * 状态
     */
    @Dict(enumClass = ReplenishmentBillStatusEnum.class)
    private String status;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 预计到货日期
     */
    private LocalDate planArrivalDate;
    /**
     * 预计可售日期
     */
    private LocalDate estimateSalesDate;

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

    /**
     * 收货渠道，店铺id/仓库id
     */
    private String receivingChannel;

    /**
     * 收货渠道，店铺id/仓库id
     */
    private String receivingChannelName;

    /**
     * 店铺预采
     */
    private Integer shopPrePurchase;
}
