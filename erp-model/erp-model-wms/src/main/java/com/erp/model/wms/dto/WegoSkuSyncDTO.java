package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * WEGO SKU 同步参数
 */
@Data
@NoArgsConstructor
public class WegoSkuSyncDTO implements Serializable {

    /**
     * WEGO SKU 批量同步入参
     */
    @Data
    @NoArgsConstructor
    public static class SyncReqDTO {
        /**
         * 海外物流商授权ID（overseas_provider.id）
         */
        @NotBlank(message = "authId不能为空")
        private String authId;

        /**
         * 平台编码（wego）
         */
        @NotBlank(message = "platform不能为空")
        private String platform;

        /**
         * 系统仓库ID（overseas_provider_warehouse.warehouse_id），由 DMP 层查询后传入
         */
        private String warehouseId;

        /**
         * 系统仓库名称，由 DMP 层查询后传入
         */
        private String warehouseName;

        /**
         * SKU 列表
         */
        @Valid
        @NotEmpty(message = "skuList不能为空")
        private List<SkuItemDTO> skuList;
    }

    /**
     * WEGO SKU 项
     */
    @Data
    @NoArgsConstructor
    public static class SkuItemDTO {
        /**
         * 平台SKU编码
         */
        @NotBlank(message = "sku不能为空")
        private String sku;

        /**
         * 产品名称
         */
        private String name;

        /**
         * 条码列表
         */
        private List<String> barcode;
    }
}
