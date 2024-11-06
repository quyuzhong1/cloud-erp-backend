package com.erp.model.wms.dto.pickingstrategy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationInventoryResultDTO {
    /**
     * 库位
     */
    private String warehouseLocation;
    /**
     * 库位id
     */
    private String warehouseLocationId;
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 仓库库区id
     */
    private String warehouseAreaId;
    /**
     * sku
     */
    private String skuId;
    /**
     * skuNo
     */
    private String skuNo;
    private String platformSkuNo;
    /**
     * 可用库存
     */
    private Integer quantity;
    /**
     * 来源明细id
     */
    private String sourceDetailId;
}
