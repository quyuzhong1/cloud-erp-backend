package com.sdk.oms.temu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class TemuWarehouseDTO {

    @JsonProperty("warehouseList")
    private List<WarehouseListDTO> warehouseList;

    @NoArgsConstructor
    @Data
    public static class WarehouseListDTO {
        @JsonProperty("defaultWarehouse")
        private Boolean defaultWarehouse;
        @JsonProperty("warehouseId")
        private String warehouseId;
        @JsonProperty("warehouseBrand")
        private String warehouseBrand;
        @JsonProperty("regionId1")
        private Integer regionId1;
        @JsonProperty("warehouseManagementType")
        private Integer warehouseManagementType;
        @JsonProperty("warehouseName")
        private String warehouseName;
    }
}
