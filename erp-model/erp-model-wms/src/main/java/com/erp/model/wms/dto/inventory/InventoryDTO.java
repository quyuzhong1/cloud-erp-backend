package com.erp.model.wms.dto.inventory;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname: InventoryDTO
 * @Description: TODO
 * @CreateTime: 2023-05-08  18:42
 * @Author: zhangchunlin
 */
public class InventoryDTO {


    /**
     * 即时库存查询条件
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {


        /**
         * sku编码
         */
        private List<String> skuNoList;


        /**
         * spu编码
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态集合
         */
        private List<String> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdLList;

        /**
         * 是否显示0库存，默认不显示
         */
        private Boolean showZeroInventory;

    }

    /**
     * 即时库存分页列表
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 库存id集合
         */
        private List<String> ids;

        /**
         * sku编号
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品图片链接
         */
        private String productImgUrl;


        /**
         * spu编号
         */
        private String spuNo;

        /**
         * 库存组织id
         */
        private String orgId;

        /**
         * 库存组织名称
         */
        private String orgName;

        /**
         * 销售状态名称
         */
        private String saleStatusName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 实际库存数量
         */
        private Integer realQty;

        /**
         * 可用库存数量
         */
        private Integer usableQty;

        /**
         * 冻结库存数量
         */
        private Integer frozenQty;

        /**
         * 在途库存数量
         */
        private Integer intransitQty;

        /**
         * 待检库存数量
         */
        private Integer waitqcQty;

    }

    /**
     * 即时库存查看流水查询条件
     */
    @Data
    @NoArgsConstructor
    public static class TransFlowSearchParamDTO extends SortDTO {

        /**
         * 即时库存id集合
         */
        private List<String> inventoryIdList;

        /**
         * 单据编号
         */
        private String sourceCode;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 单据状态
         */
        private String sourceStatus;
    }


    /**
     * 即时库存流水分页列表
     */
    @Data
    @NoArgsConstructor
    public static class TransFlowPagingViewDTO {

        /**
         * 出入库时间
         */
        private LocalDateTime tradeTime;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 单据类型
         */
        private String sourceType;

        /**
         * 单据类型名称
         */
        private String sourceTypeName;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 单据编号
         */
        private String sourceCode;

        /**
         * 单据状态名称
         */
        private String sourceStatusName;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * spu编号
         */
        private String spuNo;

        /**
         * 仓位id
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库存状态
         */
        private String inventoryStatus;

        /**
         * 库存状态名称
         */
        private String inventoryStatusName;

        /**
         * 出入库数量
         */
        private Integer qty;

        /**
         * 操作后库存数量
         */
        private Integer afterQty;

        /**
         * 批次日期
         */
        private LocalDate instockBatchDate;

    }

    /**
     * 出入库流水查询条件
     */
    @Data
    @NoArgsConstructor
    public static class InOutStockTransFlowSearchParamDTO extends SortDTO {


        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 日期范围
         */
        private List<String> dateList;

        /**
         * 单据类型集合
         */
        private List<String> sourceTypeList;


        /**
         * spu编码
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态集合
         */
        private List<String> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdLList;

    }


    /**
     * 出入库流水分页列表
     */
    @Data
    @NoArgsConstructor
    public static class InOutStockTransFlowPagingViewDTO {

        /**
         * 出入库时间
         */
        private LocalDateTime tradeTime;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 单据类型
         */
        private String sourceType;

        /**
         * 单据类型名称
         */
        private String sourceTypeName;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 单据编号
         */
        private String sourceCode;

        /**
         * 单据状态名称
         */
        private String sourceStatusName;

        /**
         * 库存组织id
         */
        private String orgId;

        /**
         * 库尊组织名称
         */
        private String orgName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * spu编号
         */
        private String spuNo;

        /**
         * 仓位id
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;


        /**
         * 库存状态
         */
        private String inventoryStatus;

        /**
         * 库存状态名称
         */
        private String inventoryStatusName;

        /**
         * 出入库数量
         */
        private Integer qty;

        /**
         * 操作后库存数量
         */
        private Integer afterQty;

        /**
         * 批次日期
         */
        private LocalDate instockBatchDate;

    }

    /**
     * 出入库列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class InOutStockSummarySearchParamDTO extends SortDTO {


        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 日期范围
         */
        private List<String> dateList;


        /**
         * spu编码
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

    }

    /**
     * 出入库列表分页列表
     */
    @Data
    @NoArgsConstructor
    public static class InOutStockSummaryPagingViewDTO {

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品图片链接
         */
        private String productImgUrl;

        /**
         * spu型号
         */
        private String spuNo;

        /**
         * 期初库存
         */
        private Integer initQty;

        /**
         * 入库汇总数量
         */
        private Integer totalInstockQty;

        /**
         * 采购入库数量
         */
        private Integer purchaseInstockQty;

        /**
         * 其他入库数量
         */
        private Integer otherInstockQty;

        /**
         * 调拨入库数量
         */
        private Integer transferInstockQty;

        /**
         * 盘盈入库数量
         */
        private Integer checkProfitInstockQty;


        /**
         * 销售退货数量
         */
        private Integer saleReturnQty;

        /**
         * 加工入库数量
         */
        private Integer machineInstockQty;

        /**
         * 出库汇总数量
         */
        private Integer totalOutstockQty;


        /**
         * 采购退货数量
         */
        private Integer purchaseReturnQty;

        /**
         * 销售出库数量
         */
        private Integer saleOutstockQty;

        /**
         * 其他出库数量
         */
        private Integer otherOutstockQty;

        /**
         * 盘亏出库数量
         */
        private Integer checkLossOutstockQty;

        /**
         * 调拨出库数量
         */
        private Integer transferOutstockQty;

        /**
         * 加工出库数量
         */
        private Integer machineOutstockQty;

    }


}