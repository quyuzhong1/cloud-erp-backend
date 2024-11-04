package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class OverseasHistoryInventoryGroupDTO {
    /**
     * ERP的SKU
     */
    private String skuId;

    /**
     * 仓库id
     */
    private String warehouseId;

    public static OverseasHistoryInventoryGroupDTO buildOverseasHistoryInventoryGroup(OverseasHistoryInventoryEntity entity) {
        OverseasHistoryInventoryGroupDTO dto = new OverseasHistoryInventoryGroupDTO();
        dto.setSkuId(entity.getSkuId());
        dto.setWarehouseId(entity.getWarehouseId());
        return dto;
    }


    public static OverseasHistoryInventoryGroupDTO buildOverseasHistoryInventoryGroup(String skuId, String warehouseId) {
        OverseasHistoryInventoryGroupDTO dto = new OverseasHistoryInventoryGroupDTO();
        dto.setSkuId(skuId);
        dto.setWarehouseId(warehouseId);
        return dto;
    }

}
