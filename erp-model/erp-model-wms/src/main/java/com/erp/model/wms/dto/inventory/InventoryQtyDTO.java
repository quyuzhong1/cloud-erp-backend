package com.erp.model.wms.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname InventoryDTO

 * @Date 2023-05-16 16:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class InventoryQtyDTO implements Serializable {


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
         * 仓库id
         */
        private String warehouseId;


        /**
         * 仓库仓位id
         */
        private String warehouseLocationId;


        /**
         * sku id
         */
        private Integer inventoryTotal;


    }


    /**
     * sku 查询库存的参数
     */
    @Data
    @NoArgsConstructor
    public static class SkuInventoryParamDTO {

        @NotNull
        @Size(min = 1,message = "sku不能为空")
        private List<String> skuIdList;

        /**
         * 仓库id
         */
        @NotNull
        @Size(min = 1,message = "仓库不能为空")
        private List<String> warehouseIdList;

        /**
         * 仓库仓位id
         */
        private List<String> warehouseLocationIdList;

        /**
         * 库存状态, 请查看枚举类 InventoryStatusEnum
         */
        @NotBlank(message = "库存状态不能为空")
        private String inventoryStatus;


    }


    /**
     * sku 查询即时库存的参数
     */
    @Data
    @NoArgsConstructor
    public static class FindSkuInventoryParamDTO {

        /**
         * sku id
         */
        @NotNull
        @Size(min = 1,message = "最少传输一条sku信息")
        private List<String> skuIds;


        /**
         * 仓库id
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;


        /**
         * 仓位id
         */
        private String warehouseLocationId;

        /**
         * 库存状态, 请查看枚举类 InventoryStatusEnum
         */
        @NotEmpty(message = "库存状态不能为空")
        private String inventoryStatus;


    }


    /**
     * sku 即时库存(带状态)
     */
    @Data
    @NoArgsConstructor
    public static class SkuInventoryStatusTotalDTO {

        /**
         * sku id
         */
        private String skuId;


        /**
         * 仓库id
         */
        private String warehouseId;


        /**
         * 仓库仓位id
         */
        private String warehouseLocationId;


        /**
         * sku id
         */
        private Integer inventoryTotal;

        /**
         * 库存状态
         */
        private String inventoryStatus;
    }



    /**
     * sku 查询库存的参数
     */
    @Data
    @NoArgsConstructor
    public static class SkuInventoryStatusParamDTO {

        @NotNull
        @Size(min = 1,message = "sku不能为空")
        private List<String> skuIdList;

        /**
         * 仓库id
         */
        @NotNull
        @Size(min = 1,message = "仓库不能为空")
        private List<String> warehouseIdList;

        /**
         * 仓库仓位id
         */
        private List<String> warehouseLocationIdList;

        /**
         * 库存状态, 请查看枚举类 InventoryStatusEnum
         */
        @NotEmpty(message = "库存状态不能为空")
        private List<String> inventoryStatusList;


    }
    /**
     * sku 查询库存的参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InventoryBySkuDTO {
        @NotNull
        @Size(min = 1,message = "sku不能为空")
        private List<String> skuIdList;
    }

}
