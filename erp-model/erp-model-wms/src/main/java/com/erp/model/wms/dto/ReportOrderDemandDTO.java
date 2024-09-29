package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@NoArgsConstructor
public class ReportOrderDemandDTO implements Serializable {


    /**
     * 列表参数
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
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;

        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;

        /**
         * 实体仓id
         */
        private String warehouseId;

        /**
         * 实体仓名称
         */
        private String warehouseName;

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
         * 剩余需求总数
         */
        private Integer totalQty;

        /**
         * B2B需求数
         */
        private Integer soQty;

        /**
         * B2C需求数
         */
        private Integer b2cSoQty;

        /**
         * 头程需求数
         */
        private Integer firstMileQty;

        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;

        /**
         * 是否缺货，true是，false否
         */
        private Boolean isVirtualScarce;

        /**
         * 缺货数量
         */
        private Integer virtualScarceQty;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
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
        * 虚拟出库名称
        */
        private String virtualWarehouseName;

        /**
        * 实体仓名称
        */
        private String warehouseName;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 剩余需求总数
        */
        private Integer totalQty;

        /**
        * B2B销售订单需求数
        */
        private Integer soQty;

        /**
        * B2C销售订单需求数
        */
        private Integer b2cSoQty;

        /**
        * 头程需求数
        */
        private Integer firstMileQty;

        /**
        * 虚拟仓可用库存
        */
        private Integer virtualUsableQty;

        /**
        * 是否缺货，true是，false否
        */
        private Boolean isVirtualScarce;

        /**
        * 缺货数量
        */
        private Integer virtualScarceQty;


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
        * 虚拟出库名称
        */
        @NotBlank(message = "虚拟出库名称不能为空")
        @Size(max = 32,message = "虚拟出库名称最大长度不能超过32位")
        private String virtualWarehouseName;

        /**
        * 实体仓名称
        */
        @NotBlank(message = "实体仓名称不能为空")
        @Size(max = 32,message = "实体仓名称最大长度不能超过32位")
        private String warehouseName;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 64,message = "产品名称最大长度不能超过64位")
        private String productName;

        /**
        * 剩余需求总数
        */
        @NotNull(message = "剩余需求总数不能为空")
        private Integer totalQty;

        /**
        * B2B销售订单需求数
        */
        @NotNull(message = "B2B销售订单需求数不能为空")
        private Integer soQty;

        /**
        * B2C销售订单需求数
        */
        @NotNull(message = "B2C销售订单需求数不能为空")
        private Integer b2cSoQty;

        /**
        * 头程需求数
        */
        @NotNull(message = "头程需求数不能为空")
        private Integer firstMileQty;

        /**
        * 虚拟仓可用库存
        */
        @NotNull(message = "虚拟仓可用库存不能为空")
        private Integer virtualUsableQty;

        /**
        * 是否缺货，true是，false否
        */
        @NotNull(message = "是否缺货，true是，false否不能为空")
        private Boolean isVirtualScarce;

        /**
        * 缺货数量
        */
        @NotNull(message = "缺货数量不能为空")
        private Integer virtualScarceQty;


    }

    /**
     * 单个查询分货参数
     */
    @Data
    @NoArgsConstructor
    public static class ViewVirtualAllocationParamDTO {
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;
        /**
         * 虚拟仓Id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;
    }

    /**
     * 单个查询分货出参
     */
    @Data
    @NoArgsConstructor
    public static class ViewVirtualAllocationDTO {

        /**
         * 虚拟仓Id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 剩余需求总数
         */
        private Integer totalQty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
        /**
         * 缺货数量
         */
        private Integer virtualScarceQty;

        /**
         * 新增分货
         */
        private AddAllocationViewDTO addAllocationViewDTO;

        /**
         * 虚拟仓调拨
         */
        private List<VirtualTransferViewDTO> virtualTransferViewList;
    }

    @Data
    @NoArgsConstructor
    public static class AddAllocationViewDTO {
        /**
         * 实体仓Id
         */
        private String warehouseId;
        /**
         * 实体仓名称
         */
        private String warehouseName;
        /**
         * 实体仓未分配数量
         */
        private Integer unDistributionQty;
    }

    @Data
    @NoArgsConstructor
    public static class VirtualTransferViewDTO {
        /**
         * 虚拟仓Id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
    }

    /**
     * 单个分货保存
     */
    @Data
    @NoArgsConstructor
    public static class AddVirtualAllocationDTO {
        /**
         * SKU
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 新增分货
         */
        @Valid
        private AddAllocationDTO addAllocationDTO;
        /**
         * 虚拟仓调拨
         */
        @Valid
        private List<VirtualTransferDTO> virtualTransferList;
    }


    @Data
    @NoArgsConstructor
    public static class AddAllocationDTO {
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
        /**
         * 分配数量
         */
        @NotNull(message = "分配数量不能为空")
        private Integer qty;
    }

    @Data
    @NoArgsConstructor
    public static class VirtualTransferDTO {
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;
        /**
         * 调入虚拟仓id
         */
        @NotBlank(message = "调入虚拟仓Id不能为空")
        private String toVirtualWarehouseId;

        /**
         * 调出虚拟仓id
         */
        @NotBlank(message = "调出虚拟仓Id不能为空")
        private String fromVirtualWarehouseId;

        /**
         * 分配数量
         */
        @NotNull(message = "分配数量不能为空")
        private Integer qty;
    }


    /**
     * 批量查询分货出参
     */
    @Data
    @NoArgsConstructor
    public static class BatchViewVirtualAllocationDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 实体仓，新增分货是和调出仓一致
         */
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 剩余需求总数
         */
        private Integer totalQty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableTotalQty;
        /**
         * 缺货数量
         */
        private Integer virtualScarceTotalQty;
        /**
         * 最大可分数（未分配数量）
         */
        private Integer unDistributionQty;
        /**
         * 调出仓,新增分货为实体仓，虚拟仓调拨为虚拟仓
         */
        private String outWarehouseId;
        /**
         * 调出仓名称
         */
        private String outWarehouseName;
        /**
         * 分货类型名称
         */
        private String typeName;
        /**
         * 分货类型
         */
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class BatchAddVirtualAllocationDTO {

        /**
         * 分货类型
         */
        @NotBlank(message = "分货类型不能为空")
        private String type;

        /**
         * skuid
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;

        /**
         * 虚拟仓id，调入虚拟仓
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;

        /**
         * 调出仓Id,新增分货为实体仓，虚拟仓调拨为虚拟仓
         */
        @NotBlank(message = "调出仓Id不能为空")
        private String outWarehouseId;

        /**
         * 分配数量
         */
        @NotNull(message = "分配数量不能为空")
        private Integer qty;
    }
}