package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 订单销量表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@NoArgsConstructor
public class ReportOrderSalesDTO implements Serializable {

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {

    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

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
        * 虚拟出库名称
        */
        private String virtualWarehouseName;

        /**
        * 实体仓名称
        */
        private String warehouseName;

        /**
        * 今日销量
        */
        private Integer todaySalesQty;

        /**
        * 昨日销量
        */
        private Integer yesterdaySalesQty;

        /**
        * 近3天销量
        */
        private Integer threeDaysSalesQty;

        /**
        * 近7天销量
        */
        private Integer sevenDaysSalesQty;

        /**
        * 近14天销量
        */
        private Integer fourteenDaysSalesQty;

        /**
        * 30天销量
        */
        private Integer thirtyDaysSalesQty;

        /**
        * 是否缺货，true是，false否
        */
        private Boolean isVirtualScarce;

        /**
        * 是否预警，true是，false否
        */
        private Boolean isWarn;

        /**
        * 缺货数量
        */
        private Integer virtualScarceQty;

        /**
        * 剩余需求总数
        */
        private Integer totalQty;

        /**
        * 虚拟仓可用库存
        */
        private Integer virtualUsableQty;

        /**
        * 虚拟仓冻结库存
        */
        private Integer virtualFrozenQty;

        /**
        * 虚拟仓库存
        */
        private Integer virtualTotalQty;

        /**
        * 已出库数量
        */
        private Integer deliveryQty;

        /**
        * 已分配数量
        */
        private Integer distributionQty;


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
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 64,message = "产品名称最大长度不能超过64位")
        private String productName;

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
        * 今日销量
        */
        @NotNull(message = "今日销量不能为空")
        private Integer todaySalesQty;

        /**
        * 昨日销量
        */
        @NotNull(message = "昨日销量不能为空")
        private Integer yesterdaySalesQty;

        /**
        * 近3天销量
        */
        @NotNull(message = "近3天销量不能为空")
        private Integer threeDaysSalesQty;

        /**
        * 近7天销量
        */
        @NotNull(message = "近7天销量不能为空")
        private Integer sevenDaysSalesQty;

        /**
        * 近14天销量
        */
        @NotNull(message = "近14天销量不能为空")
        private Integer fourteenDaysSalesQty;

        /**
        * 30天销量
        */
        @NotNull(message = "30天销量不能为空")
        private Integer thirtyDaysSalesQty;

        /**
        * 是否缺货，true是，false否
        */
        @NotNull(message = "是否缺货，true是，false否不能为空")
        private Boolean isVirtualScarce;

        /**
        * 是否预警，true是，false否
        */
        @NotNull(message = "是否预警，true是，false否不能为空")
        private Boolean isWarn;

        /**
        * 缺货数量
        */
        @NotNull(message = "缺货数量不能为空")
        private Integer virtualScarceQty;

        /**
        * 剩余需求总数
        */
        @NotNull(message = "剩余需求总数不能为空")
        private Integer totalQty;

        /**
        * 虚拟仓可用库存
        */
        @NotNull(message = "虚拟仓可用库存不能为空")
        private Integer virtualUsableQty;

        /**
        * 虚拟仓冻结库存
        */
        @NotNull(message = "虚拟仓冻结库存不能为空")
        private Integer virtualFrozenQty;

        /**
        * 虚拟仓库存
        */
        @NotNull(message = "虚拟仓库存不能为空")
        private Integer virtualTotalQty;

        /**
        * 已出库数量
        */
        @NotNull(message = "已出库数量不能为空")
        private Integer deliveryQty;

        /**
        * 已分配数量
        */
        @NotNull(message = "已分配数量不能为空")
        private Integer distributionQty;


    }


}