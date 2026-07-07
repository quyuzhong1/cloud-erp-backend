package com.sdk.tms.shopee.model.logistics.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Shopee卖家地址列表响应。
 */
@Data
public class ShopeeAddressListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "show_pickup_address")
    private Boolean showPickupAddress;

    @JSONField(name = "address_list")
    private List<ShopeeAddress> addressList;
}
