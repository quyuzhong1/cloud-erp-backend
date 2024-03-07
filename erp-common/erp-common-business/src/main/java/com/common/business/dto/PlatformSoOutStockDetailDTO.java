package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 销售出库详情DTO
 *
 * @Author Jim
 * {@code @Date} 2024/03/06
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformSoOutStockDetailDTO {

    /**
     * 平台配送明细唯一ID
     * 亚马逊=shipmentItemId
     */
    private String platformDetailUniqueId;

    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * 平台订单明细id
     */
    private String platformOrderDetailId;

    /**
     * 平台下单时间
     */
    private OffsetDateTime platformOrderCreateTime;

    /**
     * 平台付款时间
     */
    private OffsetDateTime platformPayTime;

    /**
     * 平台配送时间
     */
    private OffsetDateTime platformDeliveryTime;

    /**
     * 配送数量
     */
    private Integer qtyShipped;
}
