package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
public class VirtualInventoryDTO implements Serializable {

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
        private List<ListDetailDTO> detailList;
    }

    @Data
    @NoArgsConstructor
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

        /**
         * 实体仓在途库存
         */
        private Integer inTransitQty;

        /**
         * 实体仓待检库存
         */
        private Integer waitQcQty;
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

        /**
         * 是否过滤0实际库存，默认前端页面勾上不显示0库存
         */
        private Boolean hideZeroInventory;

    }


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        private String virtualWarehouseId;

        /**
        * sku id
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 库存状态（usable可用，frozen冻结）
        */
        private String dictInventoryStatus;

        /**
        * 数量
        */
        private Integer qty;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id 不能为空")
        @Size(max = 19,message = "仓库id 最大长度不能超过19位")
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        @NotBlank(message = "虚拟仓库id不能为空")
        @Size(max = 19,message = "虚拟仓库id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * 库存状态（usable可用，frozen冻结）
        */
        @NotBlank(message = "库存状态（usable可用，frozen冻结）不能为空")
        @Size(max = 32,message = "库存状态（usable可用，frozen冻结）最大长度不能超过32位")
        private String dictInventoryStatus;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;


    }

    @Data
    @NoArgsConstructor
    public static class ViewQtyDTO {
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 调出仓
         */
        private String fromVirtualWarehouseId;
        /**
         * 调入仓
         */
        private String toVirtualWarehouseId;
        /**
         * 调入虚拟仓可用库存
         */
        private Integer toVirtualWarehouseUsableQty = 0;
        /**
         * 调入虚拟仓实际库存
         */
        private Integer toVirtualWarehouseRealQty = 0;
        /**
         * 实体仓可分配库存
         */
        private Integer warehouseAllocationQty = 0;
        /**
         * 实体仓可用库存
         */
        private Integer warehouseUsableQty = 0;
        /**
         * 调出虚拟仓可用库存
         */
        private Integer fromVirtualWarehouseUsableQty = 0;
    }
    @Data
    @NoArgsConstructor
    public static class QtySearchDTO {

        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 调出仓
         */
        private String fromVirtualWarehouseId;
        /**
         * 调入仓
         */
        private String toVirtualWarehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class QtyTypeDTO {

        /**
         * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         */
        @NotNull(message = "类型不能为空")
        private String type;

        /**
         * 调入仓
         */
        private List<QtySearchDTO> qtySearchList;
    }

    /**
     * 即时库存查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO {

        /**
         * skuId集合
         */
        private List<String> skuIdList;


        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;


        /**
         * 虚拟仓id
         */
        private List<String> virtualWarehouseIdList;

    }

    /**
     * 即时库存查询条件
     */
    @Data
    @NoArgsConstructor
    public static class VirtualInventoryParamDTO {

        /**
         * skuId集合
         */
        @NotEmpty(message = "SKU不能为空")
        private List<String> skuIdList;


        /**
         * 仓库id集合
         */
        @NotEmpty(message = "实体仓库不能为空")
        private List<String> warehouseIdList;


        /**
         * 虚拟仓id
         */
        @NotEmpty(message = "虚拟仓不能为空")
        private List<String> virtualWarehouseIdList;

        /**
         * 库存状态
         */
        private String dictInventoryStatus;
    }

    @Data
    @NoArgsConstructor
    public static class VirtualInventoryQtyDTO {

        /**
         * SKU
         */
        private String skuId;

        /**
         * 实体仓库id
         */
        private String warehouseId;

        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;

        /**
         * 库存状态
         */
        private String dictInventoryStatus;

        /**
         * 库存数量
         */
        private Integer inventoryQty;
    }



    @Data
    @NoArgsConstructor
    public static class WarehouseStatisticsExcelDTO {
        /**
         * 主键id
         */
        private String invId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 实体仓库Id
         */
        private String warehouseId;

        /**
         * 实体仓库编码
         */
        private String warehouseCode;

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
    }


    @Data
    @NoArgsConstructor
    public static class BomParamDTO {
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;
        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

    }

    @Data
    @NoArgsConstructor
    public static class BomReturnDTO {
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;
        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;

        /**
         * bom用量
         */
        private Integer quantity;

        /**
         * bom版本
         */
        private String bomVersion;
    }


    @Data
    @NoArgsConstructor
    public static class SkuReturnDTO {
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;
        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 实体仓可用库存
         */
        private Integer usableQty;

        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
    }


    @Data
    @NoArgsConstructor
    public static class WarehouseInventoryQtyDTO {
        /**
         * skuId
         */
        private String skuId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 库存数量
         */
        private Integer qty;
    }
}