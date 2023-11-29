package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
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

    @JSONField(name ="stock_type")
    private int stockType;

    @JSONField(name ="stock_location_id")
    private String stockLocationId;

    @JSONField(name ="normal_stock")
    private int normalStock;

    @JSONField(name ="reserved_stock")
    private int reservedStock;
}
