package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ERP签收流水差异DTO（溯源）
 * @author will
 * @date 2026/3/9 11:44
 */
@Data
@NoArgsConstructor
public class AdsErpReceiveFlowDiffDetailDTO implements Serializable {


    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 主键ids
         */
        private List<String> ids;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpotParamDTO extends PagingParamDTO {
        /**
         * 主键id
         */
        private List<String> ids;
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class SourcePlatformFlowDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 操作时间
         */
        private LocalDateTime operationTime;
        /**
         * 操作单号
         */
        private String operationCode ;
        /**
         * 业务单号
         */
        private String businessCode;
        /**
         * 平台操作类型
         */
        private String platformOperationType;
        /**
         * 平台操作类型名称
         */
        private String platformOperationTypeName;
        /**
         * 标准操作类型
         */
        private String operationType;
        /**
         * 标准操作类型名称
         */
        private String operationTypeName;

        /**
         * 库存SKU
         */
        private String stockSku;

        /**
         * ERP_SKU_ID
         */
        private String skuId;

        /**
         * ERP_SKU
         */
        private String skuNo;
        /**
         * 平台产品名称
         */
        private String platformSkuName;
        /**
         * 平台仓位
         */
        private String platformWarehouseLocation;
        /**
         * 出库仓库
         */
        private String platformWarehouse;
        /**
         * 出库仓库名称
         */
        private String platformWarehouseName;
        /**
         * 仓库id
         */
        private String erpWarehouseId;
        /**
         * 仓库名称
         */
        private String erpWarehouseName;

        /**
         * 库存状态
         */
        private String inventoryStatus;
        private String inventoryStatusName;

        /**
         * 调整数量
         */
        private Integer adjustQty;
        /**
         * 备注
         */
        private String remark;

        /**
         * 参考号
         */
        private String referenceNo;

    }


    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class SourceTransferInfoDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 库存SKU
         */
        private String stockSku;

        /**
         * ERP_SKU_ID
         */
        private String skuId;

        /**
         * ERP_SKU
         */
        private String skuNo;

        /**
         * 平台产品名称
         */
        private String platformProductName;

        /**
         * 出库仓库
         */
        private String platformWarehouse;
        /**
         * 出库仓库名称
         */
        private String platformWarehouseName;
        /**
         * 仓库id
         */
        private String erpWarehouseId;
        /**
         * 仓库名称
         */
        private String erpWarehouseName;
        /**
         * 实际库存
         */
        private Integer actualQty;
        /**
         * 可用库存
         */
        private Integer usableQty;
        /**
         * 冻结库存
         */
        private Integer frozenQty;
        /**
         * 在途库存
         */
        private Integer inTransitQty;
        /**
         * 待出库库存
         */
        private Integer reservedQty;
        /**
         * 其他库存
         */
        private Integer otherQty;
    }

}