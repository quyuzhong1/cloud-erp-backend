package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * b2c扩展表
 * @author will
 * @date 2025/4/24 20:09
 */
@Data
@NoArgsConstructor
public class SoB2cCoreDTO {

    /**
     * 重新出库数据返回
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListRetryOutstockDTO {
        /**
         * 销售订单id
         */
        private String b2cSoId;
        /**
         * 销售订单编码
         */
        private String b2cSoCode;
        /**
         * 销售订单明细id
         */
        private String b2cSoDetailId;
        /**
         * 平台订单号
         */
        private String platformCode;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 国家
         */
        private String country;
        /**
         * 出库时间
         */
        private LocalDateTime outstockTime;
        /**
         * skuId
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓位
         */
        private String warehouseLocation;
    }

    /**
     * 重新出库入参
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RetryOutstockDTO {
        /**
         * 销售订单id
         */
        private String b2cSoId;
        /**
         * 销售订单明细id
         */
        private String b2cSoDetailId;
        /**
         * 国家
         */
        @NotBlank(message = "国家不能为空")
        private String country;
        /**
         * 出库时间
         */
        @NotNull(message = "出库时间不能为空")
        private LocalDateTime outstockTime;
        /**
         * 仓库Id
         */
        @NotBlank(message = "发货仓库不能为空")
        private String warehouseId;
        /**
         * 仓位
         */
        private String warehouseLocation;
    }


}
