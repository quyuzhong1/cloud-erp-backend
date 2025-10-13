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
public class PlatformFulfillOrderDetailDTO extends UniqueDto {

    /**
     * 主键id
     */
    private String  id;

    /**
     * 主表id
     */
    private String mainId;

    /**
     * sellerSku
     */
    private String msku;

    /**
     * fulfillmentNetworkSku
     */
    private String fnSku;

    /**
     * 卖家订单明细ID
     */
    private String sourceDetailId;

    /**
     * 数量
     */
    private Integer qty;
    /**
     * 发货数量
     */
    private Integer deliveryQty;

    /**
     * 发货单编码
     */
    private String code;


}
