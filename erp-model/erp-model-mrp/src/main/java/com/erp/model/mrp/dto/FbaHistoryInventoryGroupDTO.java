package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class FbaHistoryInventoryGroupDTO {
    /**
     * ERP的SKU
     */
    private String skuNo;

    /**
     * 仓库id
     */
    private String warehouseId;

    public static FbaHistoryInventoryGroupDTO buildFbaHistoryInventoryGroup(FbaHistoryInventoryEntity entity) {
        FbaHistoryInventoryGroupDTO dto = new FbaHistoryInventoryGroupDTO();
        dto.setSkuNo(entity.getSkuNo());
        dto.setWarehouseId(entity.getWarehouseId());
        return dto;
    }


    public static FbaHistoryInventoryGroupDTO buildFbaHistoryInventoryGroup(ReplenishmentSuggestionEntity entity) {
        FbaHistoryInventoryGroupDTO dto = new FbaHistoryInventoryGroupDTO();
        dto.setSkuNo(entity.getSkuNo());
        dto.setWarehouseId(entity.getFbaWarehouseId());
        return dto;
    }

}
