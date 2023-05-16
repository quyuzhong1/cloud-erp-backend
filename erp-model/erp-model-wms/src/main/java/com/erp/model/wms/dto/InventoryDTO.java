package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname InventoryDTO
 * @Description TODO
 * @Date 2023-05-16 16:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class InventoryDTO implements Serializable {


    /**
     * sku 即时库存
     */
    @Data
    @NoArgsConstructor
    public static class SkuInventoryTotalDTO {

        /**
         * sku id
         */
        private String skuId;


        /**
         * sku id
         */
        private Integer inventoryTotal;


    }


    /**
     * sku 查询即时库存的参数
     */
    @Data
    @NoArgsConstructor
    public static class findSkuInventoryParamDTO {

        /**
         * sku id
         */
        private List<String> skuIds;


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 组织id
         */
        private String orgId;

        /**
         * 仓位id
         */
        String warehouseLocationId;


    }

}
