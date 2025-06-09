package com.erp.model.mrp.vo;

import com.common.business.annotation.Dict;
import com.erp.model.mrp.enums.ReplenishmentBillStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EstimatedDeliveryVO {

    private String id;

    /**
     * 状态
     */
    @Dict(enumClass = ReplenishmentBillStatusEnum.class)
    private String status;

    /**
     * 预计发货数量
     */
    private Integer qty;

    /**
     * 预计可售日期
     */
    private LocalDate estimateSalesDate;

    /**
     * 业务类型 FBA/海外仓
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
     * 预计发货日期
     */
    private String planDeliveryDate;
    /**
     * 店铺预发数量
     */
    private Integer shopPreShipmentQty;

}
