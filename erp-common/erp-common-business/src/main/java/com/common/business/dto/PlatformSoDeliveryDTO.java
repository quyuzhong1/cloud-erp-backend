package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 发货单DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 * {@code @Date} 2024/06/11
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformSoDeliveryDTO extends UniqueDto {

    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * 销售平台
     */
    private String dictPlatform;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 明细列表
     */
    private List<PlatformSoOutStockDetailDTO> detailList;
}
