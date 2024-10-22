package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

@Data
public class Logistics implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * The identity of logistic channel
     */
    @Alias( "logistic_id")
    private Long logisticId;

    /**
     * The name of logistic channel
     */
    @Alias( "logistic_name")
    private String logisticName;

    /**
     * Whether this logistic channel is enabled on shop level.
     */
    @Alias( "enabled")
    private boolean enabled;

    /**
     * Only needed when logistics fee_type = CUSTOM_PRICE.
     */
    @Alias( "shipping_fee")
    private float shippingFee;

    /**
     * If specify logistic fee_type is SIZE_SELECTION size_id is required.
     */
    @Alias( "size_id")
    private Long sizeId;
    /**
     * when seller chooses this option, the shipping fee of this channel on item will be set to 0. Default value is False.
     */
    @Alias( "is_free")
    private boolean isFree;

    @Alias( "estimated_shipping_fee")
    private float estimatedShippingFee;
}
