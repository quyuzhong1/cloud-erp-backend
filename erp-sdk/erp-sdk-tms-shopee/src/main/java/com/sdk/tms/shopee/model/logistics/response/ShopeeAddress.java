package com.sdk.tms.shopee.model.logistics.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Shopee卖家地址信息。
 */
@Data
public class ShopeeAddress implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "address_id")
    private Long addressId;

    private String region;

    private String state;

    private String city;

    private String address;

    private String zipcode;

    private String district;

    private String town;

    @JSONField(name = "address_type")
    private List<String> addressType;
}
