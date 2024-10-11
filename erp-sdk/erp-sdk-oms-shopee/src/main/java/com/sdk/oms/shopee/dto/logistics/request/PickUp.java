package com.sdk.oms.shopee.dto.logistics.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName PickUp
 * @description: TODO
 * @date 2024年10月10日
 * @version: 1.0
 */
@Data
@Builder
public class PickUp implements Serializable {
    private Integer addressId;
    private String pickupTimeId;
    private String trackingNumber;
}
