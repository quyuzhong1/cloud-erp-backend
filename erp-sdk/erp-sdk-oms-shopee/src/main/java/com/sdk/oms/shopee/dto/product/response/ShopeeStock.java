package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShopeeStock
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class ShopeeStock implements Serializable {
    @Alias( "location_id")
    private String locationId;
    @Alias( "stock")
    private Integer stock;
}
