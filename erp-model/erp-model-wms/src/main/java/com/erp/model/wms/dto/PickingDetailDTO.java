package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/12 11:18
 */
@Data
@NoArgsConstructor
public class PickingDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 数量
         */
        @NotNull(message = "拣货数量不能为空")
        @Min(value = 1,message = "拣货数量不能小于1")
        private Integer qty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 收货仓库id
         */
        private String warehouseId;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 仓库组织id
         */
        private String orgId;

        /**
         * 仓库组织名称
         */
        private String orgName;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源单据号
         */
        private String sourceCode;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryParamDTO {

        /**
         * 仓库组织id
         */
        private String orgId;

        /**
         * 仓库组织名称
         */
        private String orgName;

        /**
         * 收货仓库id
         */
        private String warehouseId;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 数量
         */
        private Integer qty;

    }

}
