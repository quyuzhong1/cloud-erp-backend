package com.sdk.oms.tiktok.dto.tiktok.order.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientAddressBean {
    /**
     * address_detail : Unit one building 8
     * address_line1 : TikTok 5800 bristol Pkwy
     * address_line2 : Suite 100
     * address_line3 : ""
     * address_line4 : ""
     * delivery_preferences : {"drop_off_location":"Front Door"}
     * district_info : [{"address_level":"L0","address_level_name":"Country","address_name":"United Kingdom"}]
     * full_address : 1199 Coleman Ave San Jose, CA 95110
     * name : Zay
     * phone_number : (+1)213-***-1234
     * postal_code : 95110
     * region_code : US
     */

    @JsonProperty("address_detail")
    private String addressDetail;
    @JsonProperty("address_line1")
    private String addressLine1;
    @JsonProperty("address_line2")
    private String addressLine2;
    @JsonProperty("address_line3")
    private String addressLine3;
    @JsonProperty("address_line4")
    private String addressLine4;
    @JsonProperty("delivery_preferences")
    private DeliveryPreferencesBean deliveryPreferences;
    @JsonProperty("full_address")
    private String fullAddress;
    @JsonProperty("name")
    private String name;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("first_name_local_script")
    private String firstNameLocalScript;
    @JsonProperty("last_name_local_script")
    private String lastNameLocalScript;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("postal_code")
    private String postalCode;
    @JsonProperty("region_code")
    private String regionCode;
    @JsonProperty("district_info")
    private List<DistrictInfoBean> districtInfo;
}
