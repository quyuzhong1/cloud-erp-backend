package com.erp.model.mrp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import lombok.*;

import java.time.LocalDate;

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
    @TableField("warehouse_id")
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FbaInventoryResultDTO {

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 数量
         */
        private Integer qty;
        public static FbaInventoryResultDTO buildFbaInventoryResult(FbaHistoryInventoryEntity entity) {
            FbaInventoryResultDTO dto = new FbaInventoryResultDTO();
            dto.setBillDate(entity.getBillDate());
            dto.setQty(entity.getFulfillableQty());
            return dto;
        }
    }


}
