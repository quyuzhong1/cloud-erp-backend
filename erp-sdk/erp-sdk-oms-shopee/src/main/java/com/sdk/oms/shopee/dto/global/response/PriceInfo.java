package com.sdk.oms.shopee.dto.global.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName PriceInfo
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class PriceInfo implements Serializable {
    private String currency;
    private float original_price;
    private float sip_item_price;
    private String sip_item_price_source;
}
