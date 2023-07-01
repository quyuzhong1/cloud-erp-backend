package com.erp.model.wms.dto;

import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 同步
 * @author Lambda
 * @Classname SyncKingdeeB2cSoOutstockDTO
 * @Description TODO
 * @Date 2023-07-01 11:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SyncKingdeeDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class B2CSoOutstockDTO {

        /**
         * 销售出库单
         */
        private SoOutstockEntity soOutstockEntity;

        /**
         * 出入库
         */
        private InventoryInOutStockDTO inventoryInOutStock;

    }
}
