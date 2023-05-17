package com.erp.model.wms.dto.inventory;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
         * spu编码 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态集合 接口地址：plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdLList;

        /**
         * 是否显示0库存，默认勾上不显示
         */
        private Boolean hideZeroInventory;

    }

    /**
     * 即时库存导出查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ExportSearchParamDTO extends SortDTO {

        private List<String> ids;


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
        private List<Integer> saleStatusList;

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
         * sku id
         */
        private String skuId;

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
         * 销售状态编码
         */
        private Integer saleState;

        /**
         * 销售状态名称
         */
        private String saleStateName;

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

        /**
         * 实际库存=可用库存+冻结库存
         * @return
         */
        public Integer getRealQty() {
            return (Objects.nonNull(usableQty) ? usableQty : 0) + (Objects.nonNull(frozenQty) ? frozenQty : 0);
        }

    }

    /**
     * 即时库存查看流水查询条件
     */
    @Data
    @NoArgsConstructor
    public static class TransFlowSearchParamDTO extends SortDTO {

        /**
         * 仓库id（点击查看流水必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id（点击查看流水必传参数）
         */
        @NotEmpty(message = "库存组织不能为空")
        private String orgId;

        /**
         * sku id（点击查看流水必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * 单据编号
         */
        private String sourceCode;

        /**
         * 业务日期范围（单据日期）
         */
        private List<LocalDate> dateList;

        /**
         * 操作类型（对应原型单据状态）  接口地址：/wms/common/enumDropDown?type=InventoryOperationMode
         */
        private List<String> operationModeList;

        /**
         * 库存状态  接口地址：/wms/common/enumDropDown?type=InventoryTransType
         */
        private List<String> inventoryStatusList;
    }


    /**
     * 即时库存流水分页列表
     */
    @Data
    @NoArgsConstructor
    public static class TransFlowPagingViewDTO {

        /**
         * 流水id
         */
        private String id;

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
         * sku id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 单据编号
         */
        private String sourceCode;

        /**
         * 操作类型编码
         */
        private String operationMode;

        /**
         * 操作类型名称
         */
        private String operationModeName;

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
         * 单据编号
         */
        private String sourceCode;

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 日期范围
         */
        private List<String> dateList;

        /**
         * 单据名称集合
         */
        private List<String> sourceTypeList;


        /**
         * spu
         */
        private List<String> spuNoList;

        /**
         * 仓库
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织
         */
        private List<String> orgIdLList;

    }

    /**
     * 出入库流水查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ExportInOutStockTransFlowSearchParamDTO extends SortDTO {

        /**
         * 出入库流水id集合
         */
        private List<String> ids;


        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 日期范围
         */
        private List<String> dateList;

        /**
         * 单据名称
         */
        private List<String> sourceTypeList;


        /**
         * spu
         */
        private List<String> spuNoList;

        /**
         * 仓库
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织
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
         * 出入库流水id
         */
        private String id;

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
         * 单据名称
         */
        private String sourceTypeName;

        /**
         * 单据编号
         */
        private String sourceCode;


        /**
         * 操作类型编码
         */
        private String operationMode;

        /**
         * 操作类型名称
         */
        private String operationModeName;

        /**
         * sku编号
         */
        private String skuNo;


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
         * 流水id集合
         */
        private List<String> ids;


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
     * 可用库存查询参数
     */
    @Data
    @NoArgsConstructor
    public static class UsableInventoryParamDTO {
        /**
         * 组织id
         */
        @NotBlank(message = "仓库组织不能为空")
        private String orgId;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * 库位id
         */
        private String warehouseLocationId;

    }


    /**
     * 出入库列表分页列表
     */
    @Data
    @NoArgsConstructor
    public static class InOutStockSummaryPagingViewDTO {

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
        private Integer inventoryProfitInstockQty;


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
        private Integer inventoryLossOutstockQty;

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