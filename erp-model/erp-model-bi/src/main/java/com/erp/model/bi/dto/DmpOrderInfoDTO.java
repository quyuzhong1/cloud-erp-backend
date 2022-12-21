package com.erp.model.bi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:57
 */
@Data
@NoArgsConstructor
public class DmpOrderInfoDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 平台订单id
     */
    private String platformOrderId;

    /**
     * 平台名称
     */
    private String sourcePlatform;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 订单销售额[原币种]
     */
    private BigDecimal itemTotal;

    /**
     * 订单销售额[RMB-实时]
     */
    private BigDecimal cnyRealTimeAmount;

    /**
     * 订单销售额[RMB-结算]
     */
    private BigDecimal cnySettleAmount;

    /**
     * 买家姓名（下单人）
     */
    private String buyerName;

    /**
     * 买家电话1（下单电话）
     */
    private String manPhone;

    /**
     * 买家电话2（下单电话）
     */
    private String secondPhone;

    /**
     * 买家地址1（下单地址）
     */
    private String manStreet;

    /**
     * 买家地址2（下单地址）
     */
    private String secondStreet;

    /**
     * 国家名称
     */
    private String countryNameCn;

    /**
     * 订单状态 2.配货中 3.已发货 4.已完成 5.已作废
     */
    private Integer orderState;

    /**
     * 订单状态名称
     */
    private String orderStateName;

    /**
     * 订单状态 2.配货中 3.已发货 4.已完成 5.已作废
     */
    private Integer correctionStatus;

    /**
     * 订单修正状态名称
     */
    private String correctionStatusName;

    /**
     * 订单下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 订单发货时间
     */
    private Date deliveryTime;

    /**
     * 销售员
     */
    private String chargeName;

    /**
     * 子集
     */
    private List<DmpOrderItemDTO> children;
}
