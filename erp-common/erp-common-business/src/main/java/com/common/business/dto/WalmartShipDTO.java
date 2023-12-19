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
     * 物流跟踪单号
     */
    private String trackNo;

    /**
     * 发货时间
     */
    private LocalDateTime shipDateTime;

    /**
     * 明细信息
     */
    private List<WalmartShipOrderDetailDTO> detailList;
}
