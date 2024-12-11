package com.erp.model.wms.dto.inventory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.vo.PagingVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Classname: InventoryDTO
 * @CreateTime: 2023-05-08  18:42
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class InventoryDTO implements Serializable {


    /**
     * 即时库存查询条件
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 产品名称
         */
        private String productName;


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
        private List<String> orgIdList;

        /**
         * 是否过滤0实际库存，默认前端页面勾上不显示0库存
         */
        private Boolean hideZeroInventory;

        /**
         * 查询维度：warehouse仓库，warehouseArea库区，warehouseLocation仓位<br/>
         * 接口地址：/wms/dict/drop/down?type=inventoryDimension
         */
        @NotBlank
        private String dimension;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 库区名称
         */
        private String warehouseAreaName;

        /**
         * SKU ID编码集合
         */
        private List<String> skuIdList;
    }

    /**
     * 即时库存查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO extends SortDTO {


        /**
         * sku编码
         */
        private List<String> skuNoList;


        /**
         * skuId集合
         */
        private List<String> skuIdList;


        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;


        /**
         * 库位集合
         */
        private List<String> warehouseLocationList;


        /**
         * 库存组织集合
         */
        private List<String> orgIdList;


    }

    /**
     * 即时库存勾选导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportInvParamDTO implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;

        /**
         * 仓库id（勾选导出必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id（勾选导出必传参数）
         */
        @NotEmpty(message = "库存组织不能为空")
        private String orgId;
        /**
         * 库存组织id（勾选导出必传参数）
         */
        @NotEmpty(message = "库存组织不能为空")
        private String id;

        /**
         * sku id（勾选导出必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * 导出维度：warehouse，warehouseArea，warehouseLocation
         */
        @NotEmpty(message = "dimension不能为空")
        private String dimension;

        /**
         * 仓位编码（按仓位导出时传递）
         */
        private String warehouseAreaCode;

        /**
         * 库区编码（按库区导出时传递）
         */
        private String warehouseLocationCode;
    }

    /**
     * 即时库存导出查询条件
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ExportSearchParamDTO extends SortDTO {

        /**
         * 勾选行数据（仅传该字段，其他字段不要传输）
         */
        private List<ExportInvParamDTO> checkData;

        /**
         * sku id编码集合，不提供给前端使用
         */
        @JsonIgnore
        private List<String> skuIdList;

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
         * 销售状态集合 接口地址：plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdList;

        /**
         * 是否过滤0实际库存，默认前端页面勾上不显示0库存
         */
        private Boolean hideZeroInventory;

        /**
         * 查询维度：warehouse仓库，warehouseArea库区，warehouseLocation仓位
         */
        private String dimension;
        /**
         * 库区编码集合
         */
        private List<String> warehouseAreaCodeList;

        /**
         * 仓位编码集合
         */
        private List<String> warehouseLocationCodeList;

        /**
         * 库区名称
         */
        private String warehouseAreaName;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 产品名称
         */
        private String productName;
    }

    /**
     * 即时库存分页列表
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品id
         */
        private String productId;

        /**
         * 规格类型  1：无规格  2：多规格
         */
        private Integer specType;

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
         * 实际库存=可用库存+冻结库存
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
         * 金蝶库存
         */
        private String kingdeeQty;

        /**
         * 库存差异
         */
        private String diffQty;

        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 库区编码
         */
        private String warehouseArea;

        /**
         * 库区名称
         */
        private String warehouseAreaName;

    }

    /**
     * 即时库存勾选导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportInvFlowParamDTO {

        /**
         * 仓库id（勾选导出必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id（勾选导出必传参数）
         */
        @NotEmpty(message = "库存组织不能为空")
        private String orgId;

        /**
         * sku id（勾选导出必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

    }

    /**
     * 即时库存查看流水查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ExportInvFlowSearchParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 仓库id（点击查看流水必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id（点击查看流水必传参数）
         */
        private String orgId;

        /**
         * sku id（点击查看流水必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * 仓位编码
         */
        private String warehouseLocation;
    }

    /**
     * 即时库存查看流水查询条件
     */
    @Data
    @NoArgsConstructor
    public static class TransFlowSearchParamDTO extends SortDTO {


        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 仓库id（点击查看流水必传参数）
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id（点击查看流水必传参数）
         */
        private String orgId;

        /**
         * sku id（点击查看流水必传参数）
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * 仓位编码
         */
        private String warehouseLocation;
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
         * 操作时间
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
         * 数量
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
        private List<LocalDate> dateList;

        /**
         * 单据名称集合 接口地址：wms/common/enumDropDown?type=InventorySourceType
         */
        private List<String> sourceTypeList;


        /**
         * spu 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库  接口地址：/wms/warehouse/list
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态 接口地址：/plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织 接口地址： /sys/company/list
         */
        private List<String> orgIdList;

        /**
         * 是否过滤反审核数据
         * true则会过滤，false则不过滤展示所有流水数据
         */
        private Boolean hideUnApprove;

        /**
         * 单据日期范围
         */
        private List<LocalDate> billDateList;

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
        private List<LocalDate> dateList;

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
        private List<String> orgIdList;

        /**
         * 是否过滤反审核数据
         * true则会过滤，false则不过滤展示所有流水数据
         */
        private Boolean hideUnApprove;

        /**
         * 单据日期
         */
        private List<LocalDate> billDateList;

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
         * 单据编号id
         */
        private String sourceId;

        /**
         * 操作类型编码
         */
        private String operationMode;

        /**
         * 操作类型名称
         */
        private String operationModeName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;


        /**
         * 库存组织id
         */
        private String orgId;

        /**
         * 库存组织名称
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
         * 销售状态
         */
        private Integer saleState;

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

        /**
         * 金蝶同步状态
         */
        private String syncKingdeeStatus;

        /**
         * 金蝶同步状态名称
         */
        private String syncKingdeeStatusName;

    }

    /**
     * 出入库列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class InOutStockSummarySearchParamDTO extends SortDTO {

        /**
         * 日期类型 (approveDate审核日期，billDate单据日期)
         * /api/wms/dict/list，字典inventoryDate
         */
        @NotBlank(message = "日期类型不能为空")
        private String dateType;


        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 日期范围
         */
        @NotEmpty(message = "日期范围不能为空")
        private List<LocalDate> dateList;


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
     * 仓库和SKU包装实体
     */
    @Data
    @NoArgsConstructor
    public static class WareSkuDTO implements Serializable {

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * sku id
         */
        private String skuId;

    }

    /**
     * 导出出入库列表查询条件（不做勾选导出）
     */
    @Data
    @NoArgsConstructor
    public static class ExcelInOutStockSummarySearchParamDTO extends SortDTO {

        /**
         * 日期类型 (approveDate审核日期，billDate单据日期)
         * /api/wms/dict/list，字典inventoryDate
         */
        @NotBlank(message = "日期类型不能为空")
        private String dateType;

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 日期范围
         */
        @NotEmpty(message = "日期范围不能为空")
        private List<LocalDate> dateList;


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
    @AllArgsConstructor
    public static class UsableInventoryParamDTO {

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
         * 仓位编码（查询仓库下SKU的库存数量，不要传该字段；查询仓库仓位下的SKU库存数量，请传该字段）
         */
        private String warehouseLocation;

    }


    /**
     * 可用库存查询参数
     */
    @Data
    @NoArgsConstructor
    public static class UsableInventoryViewDTO {

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
         * 仓位编码（查询仓库下SKU的库存数量，不要传该字段；查询仓库仓位下的SKU库存数量，请传该字段）
         */
        private String warehouseLocation;

        /**
         * 可用库存
         */
        private Integer usableQty;

        /**
         * 冻结库存数量
         */
        private Integer frozenQty;
    }


    /**
     * 出入库列表分页列表
     */
    @Data
    @NoArgsConstructor
    public static class InOutStockSummaryPagingViewDTO {


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

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
         * spu型号
         */
        private String spuNo;

        /**
         * 期初库存
         */
        private Integer initQty;

        /**
         * 结余数量
         */
        private Integer balanceQty;

        /**
         * 入库汇总数量（入库）
         */
        private Integer totalInstockQty;

        /**
         * 采购入库数量（入库）
         */
        private Integer purchaseInstockQty;

        /**
         * 其他入库数量（入库）
         */
        private Integer otherInstockQty;

        /**
         * 调拨入库数量（入库）
         */
        private Integer transferInstockQty;

        /**
         * 盘盈入库数量（入库）
         */
        private Integer inventoryProfitInstockQty;

        /**
         * 销售退货入库数量（入库）
         */
        private Integer saleReturnQty;

        /**
         * 加工入库数量（入库）
         */
        private Integer machineInstockQty;

        /**
         * 退料入库数量（入库）
         */
        private Integer returnMaterielQty;

        /**
         * 出库汇总数量（出库）
         */
        private Integer totalOutstockQty;


        /**
         * 采购退货出库数量（出库）
         */
        private Integer purchaseReturnQty;

        /**
         * 销售出库数量（出库）
         */
        private Integer saleOutstockQty;

        /**
         * 其他出库数量（出库）
         */
        private Integer otherOutstockQty;

        /**
         * 盘亏出库数量（出库）
         */
        private Integer inventoryLossOutstockQty;

        /**
         * 调拨出库数量（出库）
         */
        private Integer transferOutstockQty;

        /**
         * 加工出库数量（出库）
         */
        private Integer machineOutstockQty;

        /**
         * 领料出库
         */
        private Integer receiveMaterielQty;

        public Integer getTotalInstockQty() {
            List<Integer> integerList = Arrays.asList(this.purchaseInstockQty , this.otherInstockQty , this.transferInstockQty , this.inventoryProfitInstockQty , this.saleReturnQty , this.machineInstockQty , this.returnMaterielQty);
            return this.totalInstockQty = integerList.stream().mapToInt(Integer::intValue).sum();
        }

        public Integer getTotalOutstockQty() {
            List<Integer> integerList = Arrays.asList(this.purchaseReturnQty , this.saleOutstockQty , this.otherOutstockQty , this.inventoryLossOutstockQty , this.transferOutstockQty , this.machineOutstockQty , this.receiveMaterielQty);
            return this.totalOutstockQty = integerList.stream().mapToInt(Integer::intValue).sum();
        }

        public Integer getPurchaseInstockQty() {
            return purchaseInstockQty;
        }

        public Integer getOtherInstockQty() {
            return otherInstockQty;
        }

        public Integer getTransferInstockQty() {
            return transferInstockQty;
        }

        public Integer getInventoryProfitInstockQty() {
            return inventoryProfitInstockQty;
        }

        public Integer getSaleReturnQty() {
            return saleReturnQty;
        }

        public Integer getMachineInstockQty() {
            return machineInstockQty;
        }

        public Integer getPurchaseReturnQty() {
            return purchaseReturnQty;
        }

        public Integer getSaleOutstockQty() {
            return saleOutstockQty;
        }

        public Integer getOtherOutstockQty() {
            return otherOutstockQty;
        }

        public Integer getInventoryLossOutstockQty() {
            return inventoryLossOutstockQty;
        }

        public Integer getTransferOutstockQty() {
            return transferOutstockQty;
        }

        public Integer getMachineOutstockQty() {
            return machineOutstockQty;
        }

        public Integer getReturnMaterielQty() {
            return returnMaterielQty;
        }

        public Integer getReceiveMaterielQty() {
            return receiveMaterielQty;
        }
    }

    /**
     * PDA首页库存结余查询
     */
    @Data
    public static class PdaHomeInventoryBalanceDTO {
        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 可用数量
         */
        private Long usableQty;

        /**
         * 今日出库数量
         */
        private Long todayDeliveryQty;

        /**
         * 今日入库数量
         */
        private Long todayStockInQty;

        public PdaHomeInventoryBalanceDTO() {
            this.usableQty = 0L;
            this.todayDeliveryQty = 0L;
            this.todayStockInQty = 0L;
        }
    }

    @Data
    @NoArgsConstructor
    public static class PdaSearchParamDTO {
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private List<String> skuIds;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 库位
         */
        private List<String> warehouseLocations;

        /**
         * 是否需要过滤组织
         */
        private boolean filterOrgFlag;

        /**
         * 是否需要过滤自建
         */
        private boolean filterSelfAddFlag;
        /**
         * 是否零库存
         */
        private boolean zeroInventory;
        /**
         * 仓位
         */
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class InventoryBySkuNoDTO {
        /**
         * 组织id
         */
        private String orgId;
        private List<String> orgIds;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku集合
         */
        private List<String> skuIds;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 是否需要过滤自建
         */
        private boolean filterSelfAddFlag;
        /**
         * 是否零库存
         */
        private boolean zeroInventory;
    }

    @Data
    @NoArgsConstructor
    public static class InventoryBySkuIdAndWarehouseDTO {
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 库位
         */
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class InventoryQtyDTO {
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 实际库存
         */
        private Integer realQty;
        /**
         * 可用库存
         */
        private Integer usableQty;
        /**
         * 冻结库存
         */
        private Integer frozenQty;
    }
    @Data
    @NoArgsConstructor
    public static class InventoryViewQtyDTO {
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 实际库存
         */
        private Integer realQty = 0;
        /**
         * 可用库存
         */
        private Integer usableQty = 0;
        /**
         * 冻结库存
         */
        private Integer frozenQty = 0;
    }

    @Data
    @NoArgsConstructor
    public static class PdaInventoryDTO {
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 仓库id
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
        /**
         * 实际库存
         */
        private Integer realQty;
        /**
         * 可用库存
         */
        private Integer usableQty;
        /**
         * 冻结库存
         */
        private Integer frozenQty;
    }

    /**
     * PDA:库存查询（SKU）
     */
    @Data
    @NoArgsConstructor
    public static class PdaInventorySearch {
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * sku名称
         */
        private String skuName;
        /**
         * spu编号
         */
        private String spuNo;
        /**
         * spu名称
         */
        private String spuName;
        /**
         * sku图片
         */
        private String imagesUrl;
        /**
         * 变体信息
         */
        private String variantProperty;
        /**
         * 总计实际库存
         */
        private Integer realTotalQty;
        /**
         * 总计可用库存
         */
        private Integer usableTotalQty;
        /**
         * 总计冻结库存
         */
        private Integer frozenTotalQty;
        /**
         * 仓库信息
         */
        private PagingVO<PdaInventoryWarehouseDTO> warehouseDTOList;
    }
    /**
     * PDA:库存查询（SKU）
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class PdaInventoryWarehousePageDTO<T> extends PagingVO<T> implements Serializable{
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 库位名称
         */
        private String warehouseLocationName;
        /**
         * 实际库存
         */
        private Integer realTotalQty;
        /**
         * 产品数量
         */
        private Integer productQty;

        public PdaInventoryWarehousePageDTO(IPage<T> page) {
            this.setList(page.getRecords());
            this.setTotalCount((int) page.getTotal());
            this.setPageSize((int) page.getSize());
            this.setCurrPage((int) page.getCurrent());
            this.setTotalPage((int) page.getPages());
        }
    }
    @Data
    @NoArgsConstructor
    public static class PdaInventoryWarehouseDTO {
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku品名名称
         */
        private String skuName;
        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 实际库存
         */
        private Integer realQty;
        /**
         * 可用库存
         */
        private Integer usableQty;
        /**
         * 冻结库存
         */
        private Integer frozenQty;
        /**
         * 仓位信息
         */
        private List<PdaInventoryWarehouseLocationDTO> warehouseLocationDTOList;

        private Integer index;
    }

    @Data
    @NoArgsConstructor
    public static class PdaInventoryPageDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * sku缩略图
         */
        private String skuImagesUrl;
        /**
         * sku品名名称
         */
        private String skuName;
        /**
         * 实际库存
         */
        private Integer realQty;
        /**
         * 仓位信息
         */
        private List<InventoryDTO.PdaInventoryWarehouseLocationDTO> warehouseLocationDTOList;

        private Integer index;
    }

    @Data
    @NoArgsConstructor
    public static class PdaInventoryWarehouseLocationDTO {
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 库位名称
         */
        private String warehouseLocationName;
        /**
         * 实际库存
         */
        private Integer realQty;
        /**
         * 可用库存
         */
        private Integer usableQty;
        /**
         * 冻结库存
         */
        private Integer frozenQty;
    }

    @Getter
    @Setter
    public static class LocationInventory {
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 库位名字
         */
        private String warehouseLocationName;
        /**
         * 可用库存
         */
        private Integer usableQty;
    }

    @Getter
    @Setter
    public static class LocationInventoryResult {
        /**
         * sku
         */
        private String skuNo;
        /**
         * sku
         */
        private String warehouseId;
        /**
         * 库位
         */
        private List<LocationInventory> locationInventory;
    }

    @Getter
    @Setter
    public static class LocationInventoryParam {
        @NotBlank(message = "sku不能为空")
        private String skuNo;
        @NotBlank(message = "仓库不能空")
        private String warehouseId;
    }

    @Getter
    @Setter
    public static class RecommendedLocationParam {
        @NotBlank(message = "sku不能为空")
        private String skuNo;
        @NotBlank(message = "仓库不能空")
        private String warehouseId;
        @NotNull(message = "数量不能空")
        private Integer usableQty;
    }

    @AllArgsConstructor
    @Data
    public static class TabDto {
        /**
         * 类型：warehouse仓库，warehouseArea库区，warehouseLocation仓位
         */
        private String tabFlag;

        /**
         * 数量
         */
        private long count;
    }
}
