package com.sdk.wms.iml.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImlLogisticChannelResp implements Serializable {


    @JSONField(name = "productCode")
    private String productCode;
    @JSONField(name = "productType")
    private String productType;
    @JSONField(name = "productName")
    private String productName;
    @JSONField(name = "prescription")
    private Integer prescription;
    @JSONField(name = "priceModel")
    private String priceModel;
    @JSONField(name = "storeWarehouse")
    private List<StoreWarehouseDTO> storeWarehouse;

    @NoArgsConstructor
    @Data
    public static class StoreWarehouseDTO {
        @JSONField(name = "warehouseCode")
        private String warehouseCode;
        @JSONField(name = "warehouseName")
        private String warehouseName;
    }
}
