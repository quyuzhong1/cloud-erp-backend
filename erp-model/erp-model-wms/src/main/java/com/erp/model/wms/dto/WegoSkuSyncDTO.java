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
        @NotBlank(message = "warehouseId不能为空")
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

        /**
         * 源端（三方仓）原始状态，如爱亚的 {@code Active}/{@code Inactive}；可选字段，不传即为空。
         * 用于映射关系回收：源端停用（非 Active）时对应已映射记录会被置为禁用。
         */
        private String status;
    }

    /**
     * SKU 同步/回收结果统计，供调用方（DMP Handler）记录日志/排查用。
     */
    @Data
    @NoArgsConstructor
    public static class ReconcileResultDTO {
        /**
         * 本次新增到未匹配对照表的记录数（仅源端启用态会新增；新增占位映射默认禁用）
         */
        private int addedCount;
        /**
         * 已映射但源端状态非启用（如爱亚 Inactive），被置为禁用的映射关系数
         */
        private int disabledCount;
        /**
         * 源端停用且未映射，被软删的 listing（及关联占位映射）数量
         */
        private int deletedCount;
    }
}
