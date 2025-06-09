package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformOrderExtendDTO implements Serializable {

    /**
     * 要求发货时间
     */
    private LocalDateTime requiredDeliveryTime;

    /**
     * 要求收货时间
     */
    private LocalDateTime requiredReceiveTime;

    /**
     * 订单来源
     */
    private String orderSourceType;
}
