package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 虚拟仓库龄
 * @author will
 * @date 2024/12/3 17:32
 */
@Data
@NoArgsConstructor
public class VirtualInventoryAgeDTO implements Serializable {

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
         * 虚拟库存明细
         */
        private List<VirtualInventoryDTO.ListDetailDTO> detailList;
    }

    /**
     * 详情
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

    @Data
    @NoArgsConstructor
    public static class ViewParamDTO {
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;
    }

    /**
     * 详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
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
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓编码
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 日期集合
         */
        private List<LocalDate> dateList;
        /**
         * 平均库龄
         */
        private List<String> avgInventoryAgeList;
    }
}
