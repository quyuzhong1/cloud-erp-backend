package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname StocktakingProfitLossDetailDTO
 * @Description
 * @Date 2023-08-10 18:41
 * @Created by yl
 */
public class StocktakingProfitLossDetailDTO implements Serializable {


    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * mainId
         */
        private String mainId;

        /**
         * skuid
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * skuName
         */
        private String productName;

        /**
         * 单位
         */
        private String unit;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 库位名称
         */
        private String warehouseLocationName;


        /**
         * 盘点数量
         */
        private Integer qty;

        /**
         * 可用数量
         */
        private Integer usableQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;


        /**
         * 差异数量
         */
        private Integer diffQty;


    }

    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {




        /**
         * skuid
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;



        /**
         * 仓库id
         *  http://172.16.100.11:3002/project/92/interface/api/17485
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;


        /**
         * 库位
         * http://172.16.100.11:3002/project/92/interface/api/13858
         */
        private String warehouseLocation;


        /**
         * 盘点数量
         */
        @NotNull (message = "盘点数量不能为空")
        private Integer qty;

        /**
         * 可用数量 http://172.16.100.11:3002/project/92/interface/api/24586
         */
        private Integer usableQty;

        /**
         * 冻结数量 http://172.16.100.11:3002/project/92/interface/api/24586
         */
        private Integer frozenQty;


        /**
         * 差异数量
         */
        private Integer diffQty;

        /**
         * 来源详情id
         */
        private String sourceDetailId;


    }


    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {


       private String id;

        /**
         * skuid
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;



        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;


        /**
         * 库位
         */
        private String warehouseLocation;


        /**
         * 盘点数量
         */
        @NotNull (message = "盘点数量不能为空")
        private Integer qty;

        /**
         * 可用数量
         */
        private Integer usableQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;


        /**
         * 差异数量
         */
        private Integer diffQty;




    }

    @Data
    @NoArgsConstructor
    public static class LastDTO {
        /**
         * 单据号
         */
        private String code;
        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
         * 库存组织
         */
        private String warehouseOrgId;
        /**
         * 仓库
         */
        private String warehouseId;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * skuId
         */
        private String skuId;
    }


    @Data
    @NoArgsConstructor
    public static class ParamsDTO {
        /**
         * 仓库
         */
        private List<String> warehouseId;
        /**
         * 库存组织
         */
        private List<String> warehouseOrgId;
        /**
         * skuId
         */
        private List<String> skuId;
    }
}
