package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 虚拟仓调整单明细表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
*/
@Data
@NoArgsConstructor
public class VirtualAdjustDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * 实收数量
        */
        private Integer qty;

        /**
        * 备注
        */
        private String remark;

        /**
        * 库存状态
        */
        private String inventoryStatus;
        private String inventoryStatusName;

        /**
        * 类型
        */
        private String type;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 虚拟仓id
        */
        private String virtualWarehouseId;

        /**
        * 虚拟仓库名称
        */
        private String virtualWarehouseName;

        /**
        * 仓库名称
        */
        private String warehouseName;


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
        * 主表id
        */
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

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
        * 实收数量
        */
        @NotNull(message = "实收数量不能为空")
        private Integer qty;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 库存状态
         * InventoryStatusEnum
        */
        @NotBlank(message = "库存状态不能为空")
        @Size(max = 32,message = "库存状态最大长度不能超过32位")
        private String inventoryStatus;

        /**
        * 类型  根据数量进行判断
         * 负数为扣减，正数为增加
         * InventoryInOutEnum
        */
        @Size(max = 32,message = "类型最大长度不能超过32位")
        private String type;

        /**
        * 产品名称
        */
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String productName;

        /**
        * 仓库id
        */
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 虚拟仓id
        */
        @NotBlank(message = "虚拟仓id不能为空")
        @Size(max = 19,message = "虚拟仓id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * 虚拟仓库名称
        */
        @NotBlank(message = "虚拟仓库名称不能为空")
        @Size(max = 255,message = "虚拟仓库名称最大长度不能超过255位")
        private String virtualWarehouseName;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 255,message = "仓库名称最大长度不能超过255位")
        private String warehouseName;


    }


}