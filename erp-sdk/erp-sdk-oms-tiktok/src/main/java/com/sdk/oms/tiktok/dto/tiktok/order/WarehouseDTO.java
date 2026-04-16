package com.sdk.oms.tiktok.dto.tiktok.order;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class WarehouseDTO {

    @JSONField(name = "code")
    private Integer code;
    @JSONField(name = "data")
    private DataDTO data;
    @JSONField(name = "message")
    private String message;
    @JSONField(name = "request_id")
    private String requestId;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JSONField(name = "warehouses")
        private List<WarehousesDTO> warehouses;

        @NoArgsConstructor
        @Data
        public static class WarehousesDTO {
            @JSONField(name = "address")
            private AddressDTO address;
            @JSONField(name = "effect_status")
            private String effectStatus;
            @JSONField(name = "entity_id")
            private String entityId;
            @JSONField(name = "id")
            private String id;
            @JSONField(name = "is_default")
            private Boolean isDefault;
            @JSONField(name = "name")
            private String name;
            @JSONField(name = "sub_type")
            private String subType;
            @JSONField(name = "type")
            private String type;

            @NoArgsConstructor
            @Data
            public static class AddressDTO {
                @JSONField(name = "address_line1")
                private String addressLine1;
                @JSONField(name = "city")
                private String city;
                @JSONField(name = "contact_person")
                private String contactPerson;
                @JSONField(name = "distict")
                private String distict;
                @JSONField(name = "full_address")
                private String fullAddress;
                @JSONField(name = "phone_number")
                private String phoneNumber;
                @JSONField(name = "postal_code")
                private String postalCode;
                @JSONField(name = "region")
                private String region;
                @JSONField(name = "region_code")
                private String regionCode;
                @JSONField(name = "state")
                private String state;
                @JSONField(name = "town")
                private String town;
            }
        }
    }
}
