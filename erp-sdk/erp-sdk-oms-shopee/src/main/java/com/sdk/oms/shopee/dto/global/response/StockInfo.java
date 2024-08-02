package com.sdk.oms.shopee.dto.global.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName StockInfo
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class StockInfo implements Serializable {

    @Alias("stock_type")
    private int stockType;

    @Alias("stock_location_id")
    private String stockLocationId;

    @Alias("normal_stock")
    private int normalStock;

    @Alias("reserved_stock")
    private int reservedStock;
}
