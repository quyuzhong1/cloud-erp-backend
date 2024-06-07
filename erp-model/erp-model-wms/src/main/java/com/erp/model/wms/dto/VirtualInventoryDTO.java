package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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
        private Integer index;
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

        /**
         * 虚拟库存明细
         */
        private List<ListDetailDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class ListDetailDTO {

        /**
         * 序号（可用仓库id）
         */
        private String index;
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
         * 虚拟仓可用库存
         */
        private Integer virtualFrozenQty;

    }

    @Data
    @NoArgsConstructor
    public static class ListInventoryDTO {

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
         * 实体仓数量
         */
        private Integer qty;

        /**
         * 虚拟仓数量
         */
        private Integer virtualQty;
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
}