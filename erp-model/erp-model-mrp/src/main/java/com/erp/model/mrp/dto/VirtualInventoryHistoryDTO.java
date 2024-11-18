package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 虚拟库存表请求响应实体
 */
@Getter
@Setter
public class VirtualInventoryHistoryDTO {
    /**
     * 分页显示数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 序号
         */
        private String indexId;
        /**
         * skuId【可排序】
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
         * 虚拟仓库存
         */
        private Integer virtualQty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓冻结库存
         */
        private Integer virtualFrozenQty;
        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 虚拟库存明细
         */
        private List<ListDetailDTO> detailList;
    }



    @Getter
    @Setter
    public static class ListDetailDTO {

        /**
         * 序号
         */
        private String indexId;
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
         * 实体仓冻结库存
         */
        private Integer frozenQty;
        /**
         * 实体仓实际库存
         */
        private Integer realQty;
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
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓冻结库存
         */
        private Integer virtualFrozenQty;

    }

    @Getter
    @Setter
    public static class SearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 是否过滤0实际库存，默认前端页面勾上不显示0库存
         */
        private Boolean hideZeroInventory;

    }

    @Data
    @NoArgsConstructor
    public static class ListInventoryDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * 仓库Id
         */
        private String warehouseId;

        /**
         * 虚拟仓库Id
         */
        private String virtualWarehouseId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 仓库状态
         */
        private String dictInventoryStatus;

        /**
         * 虚拟仓数量
         */
        private Integer virtualQty;

        /**
         * 实体仓可用数量
         */
        private Integer usableQty;

        /**
         * 实体仓冻结数量
         */
        private Integer frozenQty;
    }
}
