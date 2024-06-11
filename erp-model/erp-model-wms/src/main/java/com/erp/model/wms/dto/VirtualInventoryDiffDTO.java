package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 虚拟库存表请求响应实体
 * @author will
 * @since 2024-06-03
*/
@Data
@NoArgsConstructor
public class VirtualInventoryDiffDTO implements Serializable {


    /**
     * 分页查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }


    /**
     * 分页显示数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * skuId【可排序】
         */
        private String skuId;
        /**
         * SKU【可排序】
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 实体仓库【可排序】
         */
        private String warehouseId;
        /**
         * 实体仓库名称
         */
        private String warehouseName;
        /**
         * 实体仓可用库存
         */
        private Integer usableQty;
        /**
         * 实体仓已分配数
         */
        private Integer distributionQty;
        /**
         * 实体仓未分配数
         */
        private Integer unDistributionQty;
        /**
         * 虚拟仓库存
         */
        private Integer virtualQty;
    }

    /**
     * 差异明细
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDetailDTO extends SortDTO {
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * 仓库Id
         */
        @NotBlank(message = "实体仓库Id不能为空")
        private String warehouseId;
    }


    /**
     * 差异明细
     */
    @Data
    @NoArgsConstructor
    public static class ListDetailDTO {

        /**
         * 实体仓已分配数
         */
        private Integer distributionQty;
        /**
         * 实体仓可用库存
         */
        private Integer usableQty;
        /**
         * 库存差异数量
         */
        private Integer diffQty;
        /**
         * 明细信息
         */
        private List<ListDetailQtyDTO> detailList;
    }

    /**
     * 差异明细数量
     */
    @Data
    @NoArgsConstructor
    public static class ListDetailQtyDTO {
        /**
         * 虚拟仓库【可排序】
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库编号
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓库名称
         */
        private String virtualWarehouseName;
        /**
         * 虚拟仓库存【可排序】
         */
        private Integer virtualQty;
        /**
         * 虚拟仓可用库存【可排序】
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓可用库存【可排序】
         */
        private Integer virtualFrozenQty;
    }

    /**
     * 差异导出数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDiffExportDataDTO {
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
         * 实体仓库
         */
        private String warehouseId;
        /**
         * 实体仓库名称
         */
        private String warehouseName;
        /**
         * 实体仓可用库存
         */
        private Integer usableQty;
        /**
         * 实体仓已分配数
         */
        private Integer distributionQty;
        /**
         * 实体仓未分配数
         */
        private Integer unDistributionQty;
        /**
         * 虚拟仓库
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库编号
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓库名称
         */
        private String virtualWarehouseName;
        /**
         * 虚拟仓库存
         */
        private Integer virtualQty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualFrozenQty;
    }



}