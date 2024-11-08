package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
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

        /**
         * 库存差异,true是，false否
         */
        private Boolean isDiff;

        /**
         * 超出分配,true是，false否
         */
        private Boolean isExceed;

        /**
         * 仅看虚拟仓相关
         */
        private Boolean isViewVirtual;

        /**
         * 去除0库存
         */
        private Boolean isDeleteZeroInventory;
    }


    /**
     * 分页显示数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
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
         * 实体仓冻结库存
         */
        private Integer frozenQty;
        /**
         * 实体仓在途库存
         */
        private Integer inTransitQty;
        /**
         * 实体仓待检库存
         */
        private Integer waitQcQty;

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
         * 实体仓已下推数量
         */
        private Integer totalVirtualQty;
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
         * 库存差异，true是，false否
         */
        private Boolean isDiff;
        /**
         * 库存差异，true是，false否
         */
        private String isDiffName;
        /**
         * 超出分配
         */
        private Boolean isExceed;

        /**
         * 超出分配数量
         */
        private Integer exceedQty;
    }

    /**
     * 差异明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
         * SKU【可排序】
         */
        private String skuId;
        /**
         * 实体仓库【可排序】
         */
        private String warehouseId;
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
         * 虚拟仓冻结库存【可排序】
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
         * 实体仓实际库存
         */
        private Integer realQty;
        /**
         * 实体仓可用库存
         */
        private Integer usableQty;
        /**
         * 实体仓冻结库存
         */
        private Integer frozenQty;
        /**
         * 实体仓在途库存
         */
        private Integer inTransitQty;
        /**
         * 实体仓待检库存
         */
        private Integer waitQcQty;
        /**
         * 实体仓已分配数
         */
        private Integer distributionQty;
        /**
         * 实体仓未分配数
         */
        private Integer unDistributionQty;
        /**
         * 库存差异
         */
        private Boolean isDiff;
        /**
         * 库存差异名称
         */
        private String isDiffName;
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
         * 实体仓下虚拟仓总库存
         */
        private Integer totalVirtualQty;
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


    /**
     * 一键调整保存
     */
    @Data
    @NoArgsConstructor
    public static class UpdateVirtualInventoryDTO {

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;

        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 0, message = "数量不能小于0")
        @Max(value = 999999999,message = "数量最大值为999999999" )
        private Integer qty;
    }

    /**
     * 查询推荐数量
     */
    @Data
    @NoArgsConstructor
    public static class ListSuggestQtyParamDTO {

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;

        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class ListSuggestQtyDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * 仓库Id
         */
        private String warehouseId;

        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;

        /**
         * 推荐数量
         */
        private Integer qty;
    }
}