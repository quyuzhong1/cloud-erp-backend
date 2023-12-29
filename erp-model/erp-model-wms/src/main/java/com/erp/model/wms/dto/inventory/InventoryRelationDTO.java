package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.entity.InventoryDetailEntity;
import com.erp.model.wms.entity.InventoryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRelationDTO implements Serializable {

    /**
     * inventory id
     */
    private InventoryEntity inventory;

    /**
     *inventory_his id
     */
    private InventoryDetailEntity inventoryDetail;

}