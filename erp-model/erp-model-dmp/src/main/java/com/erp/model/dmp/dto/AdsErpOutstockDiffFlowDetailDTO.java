package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
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
    public static class PagingParamDTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

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
        private String platformProductName;
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
    public static class SourcePlatformDTO {
        /**
         * 主键id
         */
        private String id;

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
        /**
         * 销售平台
         */
        private String salesPlatform;

        /**
         * 销售平台名称
         */
        private String salesPlatformName;

        /**
         * ERP销售平台
         */
        private String erpSalesPlatform;
        /**
         * ERP销售平台名称
         */
        private String erpSalesPlatformName;
        /**
         * ERP店铺ID
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 单据日期
         */
        private String platformBillDate;

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
         * 平台单据状态
         */
        private String platformBillStatus;
        /**
         * 平台单据状态名称
         */
        private String platformBillStatusName;
        /**
         * 平台单据状态
         */
        private String erpBillStatus;
        /**
         * 标准单据状态名称
         */
        private String erpBillStatusName;
        /**
         * 跟踪号
         */
        private String trackNo;

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
         * 出库数量
         */
        private Integer outstockQty;

        /**
         * 平台产品名称
         */
        private String platformProductName;
    }
}