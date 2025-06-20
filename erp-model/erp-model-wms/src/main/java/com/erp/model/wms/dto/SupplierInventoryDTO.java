package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class SupplierInventoryDTO implements Serializable {

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 是否隐藏0库存
         */
        private Boolean hideZeroInventory;

    }
    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 产品图片【可排序】
         */
        private String imagesUrl;
        /**
         * 产品图片路径【可排序】
         */
        private String imagesUrlPath;
        /**
         * skuId【可排序】
         */
        private String skuId;
        /**
         * SKU编码【可排序】
         */
        private String skuNo;
        /**
         * 产品名称【可排序】
         */
        private String productName;
        /**
         * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架【可排序】
         */
        private Integer saleState;
        /**
         * 销售状态名称
         */
        private String saleStateName;
        /**
         * 仓库Id【可排序】
         */
        private String warehouseId;
        /**
         * 仓库名称【可排序】
         */
        private String warehouseName;
        /**
         * 实际库存【可排序】
         */
        private Integer realQty;
        /**
         * 可用库存【可排序】
         */
        private Integer useQty;
        /**
         * 冻结库存【可排序】
         */
        private Integer freezeQty;
        /**
         * 在途库存【可排序】
         */
        private Integer inTransitQty;
    }
}
