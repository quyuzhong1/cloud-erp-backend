package com.erp.tms.aliexpress.model.query.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 速卖通海外托管可用线路响应
 */
@Data
public class OverseasManagedShippingServiceResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private DataDTO data;
    private Object success;
    private String errorMessage;
    private String errorCode;
    @JSONField(name = "request_id", alternateNames = {"requestId"})
    private String requestId;

    @Data
    public static class DataDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<ShipmentProviderDTO> unusableShipmentProviderList;
        private List<ShipmentProviderDTO> shipmentProviderDTOList;
    }

    @Data
    public static class ShipmentProviderDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<ShippingTypeDTO> shippingTypes;
        private String shipmentProviderName;
        private String deliveryMode;
        private List<CarrierInfoDTO> carrierInfoDTOList;
        private String shipmentProviderCode;
        private String unusableReason;
    }

    @Data
    public static class ShippingTypeDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String code;
        private String name;
    }

    @Data
    public static class CarrierInfoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String carrierName;
        private String carrierCode;
    }
}
