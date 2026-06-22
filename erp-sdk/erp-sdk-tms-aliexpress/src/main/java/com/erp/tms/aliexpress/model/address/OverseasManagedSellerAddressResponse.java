package com.erp.tms.aliexpress.model.address;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 速卖通海外托管卖家地址响应
 */
@Data
public class OverseasManagedSellerAddressResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private ResultDTO result;
    @JSONField(name = "request_id", alternateNames = {"requestId"})
    private String requestId;

    @Data
    public static class ResultDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private DataDTO data;
        private Object success;
        private String errorMessage;
        private String errorCode;
    }

    @Data
    public static class DataDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<SellerAddressDTO> pickupSellerAddressList;
        private List<SellerAddressDTO> refundSellerAddressList;
    }

    @Data
    public static class SellerAddressDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String area;
        private String country;
        private String city;
        private String addressId;
        private String localityIdentifier;
        private String province;
        private String phone;
        private String streetAddress;
        private String street;
        private String name;
        private String postCode;
        private String memberType;
        private String email;
        private Object defaultAddress;
    }
}
