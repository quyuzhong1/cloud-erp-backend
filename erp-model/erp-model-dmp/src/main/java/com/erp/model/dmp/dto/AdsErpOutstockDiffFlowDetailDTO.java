package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 第三方仓出库单据差异表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-12
*/
@Data
@NoArgsConstructor
public class AdsErpOutstockDiffFlowDetailDTO implements Serializable {


	/**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 仓库ID
         */
        private String warehouseId;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpotParamDTO extends PagingParamDTO{
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
     public static class SourceSelfDTO {
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
         * 出库仓库id
         */
        private String outstockWarehouseId;
        /**
         * 出库仓库名称
         */
        private String outstockWarehouseName;
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
    public static class SourcePlatformDTO {

        /**
         * 出库单号
         */
        private String outstockCode;

        /**
         * ERP下单单号
         */
        private String soDeliveryCode;

        /**
         * 平台原始订单号
         */
        private String platformOrderCode;

        /**
         * ERP销售单号
         */
        private String soCode;
    }
}