package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 虚拟仓分货单明细请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
*/
@Data
@NoArgsConstructor
public class VirtualWarehouseAllocationDetailDTO implements Serializable {




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
        * 是否失效 true 失效 false 未失效
        */
        private Boolean disabled;

        /**
        * 分货主单id
        */
        private String mainId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 调出虚拟仓id
        */
        private String fromVirtualWarehouseId;

        /**
        * 调出虚拟仓编码
        */
        private String fromVirtualWarehouseCode;

        /**
        * 调出虚拟仓名称
        */
        private String fromVirtualWarehouseName;

        /**
        * 调出数量/调拨数量
        */
        private Integer qty;

        /**
        * 调入虚拟仓id
        */
        private String toVirtualWarehouseId;

        /**
        * 调入虚拟仓编码
        */
        private String toVirtualWarehouseCode;

        /**
        * 调出入虚拟仓名称
        */
        private String toVirtualWarehouseName;

        /**
        * 完结说明
        */
        private String finishDescription;

        /**
        * 第三方单据单号
        */
        private String thirdCode;


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
        * 是否失效 true 失效 false 未失效
        */
        @NotNull(message = "是否失效 true 失效 false 未失效不能为空")
        private Boolean disabled;

        /**
        * 分货主单id
        */
        @NotBlank(message = "分货主单id不能为空")
        @Size(max = 19,message = "分货主单id最大长度不能超过19位")
        private String mainId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 200,message = "仓库名称最大长度不能超过200位")
        private String warehouseName;

        /**
        * 调出虚拟仓id
        */
        @NotBlank(message = "调出虚拟仓id不能为空")
        @Size(max = 19,message = "调出虚拟仓id最大长度不能超过19位")
        private String fromVirtualWarehouseId;

        /**
        * 调出虚拟仓编码
        */
        @NotBlank(message = "调出虚拟仓编码不能为空")
        @Size(max = 30,message = "调出虚拟仓编码最大长度不能超过30位")
        private String fromVirtualWarehouseCode;

        /**
        * 调出虚拟仓名称
        */
        @NotBlank(message = "调出虚拟仓名称不能为空")
        @Size(max = 200,message = "调出虚拟仓名称最大长度不能超过200位")
        private String fromVirtualWarehouseName;

        /**
        * 调出数量/调拨数量
        */
        @NotNull(message = "调出数量/调拨数量不能为空")
        private Integer qty;

        /**
        * 调入虚拟仓id
        */
        @NotBlank(message = "调入虚拟仓id不能为空")
        @Size(max = 19,message = "调入虚拟仓id最大长度不能超过19位")
        private String toVirtualWarehouseId;

        /**
        * 调入虚拟仓编码
        */
        @NotBlank(message = "调入虚拟仓编码不能为空")
        @Size(max = 30,message = "调入虚拟仓编码最大长度不能超过30位")
        private String toVirtualWarehouseCode;

        /**
        * 调出入虚拟仓名称
        */
        @NotBlank(message = "调出入虚拟仓名称不能为空")
        @Size(max = 200,message = "调出入虚拟仓名称最大长度不能超过200位")
        private String toVirtualWarehouseName;

        /**
        * 完结说明
        */
        @NotBlank(message = "完结说明不能为空")
        @Size(max = 255,message = "完结说明最大长度不能超过255位")
        private String finishDescription;

        /**
        * 第三方单据单号
        */
        @NotBlank(message = "第三方单据单号不能为空")
        @Size(max = 255,message = "第三方单据单号最大长度不能超过255位")
        private String thirdCode;


    }


    /**
     * 分货信息
     */
    @Data
    @NoArgsConstructor
    public static class AllocationDataDTO {
        /**
         * 类型
         */
        private String type;
        /**
         * sku信息
         */
        private String skuId;
        /**
         * 实体仓id
         */
        private String warehouseId;
        /**
         * 调出虚拟仓id
         */
        private String fromVirtualWarehouseId;
        /**
         * 调入虚拟仓id
         */
        private String toVirtualWarehouseId;
        /**
         * 数量
         */
        private Integer qty;
    }
}