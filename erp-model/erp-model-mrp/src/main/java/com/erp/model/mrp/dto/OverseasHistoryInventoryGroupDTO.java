package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
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

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class InventoryDTO {

        /**
         * 平台仓库编码
         */
        private String warehouseCode;
        /**
         * 平台类型: goodcang=谷仓，iml=艾姆勒
         */
        private String dictPlatform;

        /**
         * ERP的SKU ID
         */
        private String skuId;
    }
}
