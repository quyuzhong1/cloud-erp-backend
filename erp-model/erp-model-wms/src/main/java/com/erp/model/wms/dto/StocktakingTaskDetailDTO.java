package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Lambda
 * @Classname StocktakingTaskDetailDTO
 * @Description
 * @Date 2023-08-03 16:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StocktakingTaskDetailDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 详情id
         */
        private String id;


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名
         */
        private String warehouseName;


        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * sku 名称
         */
        private String productName;


        /**
         * 可用数量
         */
        private Integer usableQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;

        /**
         * 盘点数量
         */
        private Integer qty;

        /**
         * 差异数量
         */
        private Integer diffQty;

        /**
         * 初盘数量
         */
        private Integer firstQty;


        /**
         * 下推盘盈盘亏状态
         */
        private String pushStocktakingProfitLossStatus;


        /**
         * 下推盘盈盘亏状态名称
         */
        private String pushStocktakingProfitLossStatusName;
    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        @NotBlank(message = "任务明细不能为空")
        private String id;

        @NotNull(message = "盘点数量不能为空")
        @DecimalMin(value = "0",message ="盘点数量最小为0" )
        private Integer qty;

        /**
         * 初盘数量
         */
        private Integer firstQty;
    }

    /**
     * 导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {

        /**
         * 详情id
         */
        private String id;




        private String code;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名
         */
        private String warehouseName;


        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * sku 名称
         */
        private String productName;





        /**
         * 盘点方式名
         */
        private String stocktakingModeName;

        /**
         * 可用数量
         */
        private Integer usableQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;

        /**
         * 盘点数量
         */
        private Integer qty;

        /**
         * 差异数量
         */
        private Integer diffQty;


        /**
         * 盘点人
         */
        private String stocktakingUserName;

        /**
         * 初盘数量
         */
        private Integer firstQty;

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
}
