package com.sdk.oms.shopee.dto.order.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.sdk.oms.shopee.dto.base.AddressBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RecipientAddress extends AddressBase {

    public static final long serialVersionUID = 1L;

    /**
     * Recipient's name for the address.
     */
    private String name;
    /**
     * Recipient's phone number input when order was placed.
     */
    private String phone;
    private String town;
    private String district;
    private String city;
    private String state;
    private String region;
    private String zipcode;

    @JSONField(name = "full_address")
    private String fullAddress;


}
