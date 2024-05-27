package com.sdk.oms.shopee.dto.order.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderItemSimple implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * Shopee's unique identifier for a discount item.
     */
    @Alias( "item_id")
    private Long itemId;

    /**
     * Shopee's unique identifier for a variation of an item.
     */
    @Alias( "model_id")
    protected Long modelId;

    /**
     * Shopee's unique identifier for a variation of an item.
     */
    @Alias( "model_quantity")
    protected Long modelQuantity;

}
