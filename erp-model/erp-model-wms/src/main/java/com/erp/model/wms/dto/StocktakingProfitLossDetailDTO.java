package com.erp.model.wms.dto;

import com.erp.model.wms.enums.BillTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
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
}
