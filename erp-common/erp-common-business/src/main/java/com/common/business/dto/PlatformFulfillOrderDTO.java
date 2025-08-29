package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * FBA货件DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @author Jim
 * @date 2023/11/1
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformFulfillOrderDTO extends UniqueDto {

    /**
     * 主键id
     */
    private String id;
    private String shopId;

    /**
     * 卖家订单编号
     */
    private String code;

    /**
     * 亚马逊订单编号
     */
    private String platformCode;

    /**
     * 发货状态
     */
    private String deliveryStatus;

    /**
     * 发货时间（东八区）
     */
    private LocalDateTime deliveryTime;

    /**
     * 物流跟踪号
     */
    private String trackNo;

    /**
     * 订单类型（soMultiChannel多渠道）
     */
    private String orderType;
    private String orderStatus;

    /**
     * 货件id
     */
    private String shipmentId;


}
