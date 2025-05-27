package com.sdk.oms.temu.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class TemuLogisticShipmentDTO {

    @JSONField(name = "shipmentInfoDTO")
    private List<ShipmentInfoDTODTO> shipmentInfoDTO;

    @NoArgsConstructor
    @Data
    public static class ShipmentInfoDTODTO {
        @JSONField(name = "quantity")
        private Integer quantity;
        @JSONField(name = "carrierName")
        private String carrierName;
        @JSONField(name = "packageDeliveryType")
        private Integer packageDeliveryType;
        @JSONField(name = "packageSn")
        private String packageSn;
        @JSONField(name = "cooperativeWarehouseDTO")
        private CooperativeWarehouseDTODTO cooperativeWarehouseDTO;
        @JSONField(name = "carrierId")
        private Integer carrierId;
        @JSONField(name = "trackingWarningLabel")
        private Integer trackingWarningLabel;
        @JSONField(name = "trackingNumber")
        private String trackingNumber;
        @JSONField(name = "skuId")
        private Long skuId;
        @JSONField(name = "subPackageShipmentInfoList")
        private Object subPackageShipmentInfoList;

        @NoArgsConstructor
        @Data
        public static class CooperativeWarehouseDTODTO {
            @JSONField(name = "warehouseProviderBrandName")
            private String warehouseProviderBrandName;
            @JSONField(name = "warehouseName")
            private String warehouseName;
            @JSONField(name = "warehouseProviderCode")
            private String warehouseProviderCode;
            @JSONField(name = "warehouseCode")
            private String warehouseCode;
        }
    }
}
