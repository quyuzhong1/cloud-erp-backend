package com.erp.model.wms.dto;

import com.erp.model.wms.dto.inventory.InventoryInOutStockRuleDTO;
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
         * 是否首次
         * true 是 false 不是
         */
        private Boolean isFirst;

        /**
         * flagId 标识id 当不为空就要删除
         *
         */
        private String flagId;

        /**
         * 出入库
         */
        private InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO;

    }
}
