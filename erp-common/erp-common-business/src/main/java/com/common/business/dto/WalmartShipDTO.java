package com.common.business.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WalmartShipDTO {

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 第三方平台订单号
     */
    private String platformCode;

    /**
     * ERP订单编号
     */
    private String soCode;

    /**
     * 物流运单号
     */
    private String transportNo;

    /**
     * 物流跟踪单号
     */
    private String trackNo;

    /**
     * 发货时间
     */
    private LocalDateTime shipDateTime;

    /**
     * 物流渠道id
     */
    private String logisticsChannelId;

    /**
     * 物流商code
     */
    private String logisticsPlatformCode;

    /**
     *  标记发货订单类型（transportNo运单号、trackNo跟踪号）
     */
    private String orderDeliveryMarkType;

    /**
     * 明细信息
     */
    private List<WalmartShipOrderDetailDTO> detailList;
}
