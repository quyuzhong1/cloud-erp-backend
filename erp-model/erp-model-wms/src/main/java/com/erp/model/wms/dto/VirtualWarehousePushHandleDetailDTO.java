package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 分货单拆单明细表请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
*/
@Data
@NoArgsConstructor
public class VirtualWarehousePushHandleDetailDTO implements Serializable {




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
        * 同步状态-0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败
        */
        private String status;

        /**
        * 分货单id
        */
        private String allocationId;

        /**
        * 仓库id
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
        * 第三方调出虚拟仓id
        */
        private String thirdFromVirtualWarehouseId;

        /**
        * 第三方调入虚拟仓id
        */
        private String thirdToVirtualWarehouseId;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
        */
        private String type;

        /**
        * 方向
        */
        private Integer direction;

        /**
        * 第三方单号
        */
        private String code;

        /**
        * 系统类型：wdt旺店通
        */
        private String sysType;

        /**
        * 分货单拆单主表id
        */
        private String handleId;


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
        * 同步状态-0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败
        */
        @NotBlank(message = "同步状态不能为空")
        @Size(max = 16,message = "同步状态最大长度不能超过16位")
        private String status;

        /**
        * 分货单id
        */
        @NotBlank(message = "分货单id不能为空")
        @Size(max = 19,message = "分货单id最大长度不能超过19位")
        private String allocationId;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 调出虚拟仓id
        */
        @NotBlank(message = "调出虚拟仓id不能为空")
        @Size(max = 19,message = "调出虚拟仓id最大长度不能超过19位")
        private String fromVirtualWarehouseId;

        /**
        * 调入虚拟仓id
        */
        @NotBlank(message = "调入虚拟仓id不能为空")
        @Size(max = 19,message = "调入虚拟仓id最大长度不能超过19位")
        private String toVirtualWarehouseId;

        /**
        * 第三方调出虚拟仓id
        */
        @NotBlank(message = "第三方调出虚拟仓id不能为空")
        @Size(max = 19,message = "第三方调出虚拟仓id最大长度不能超过19位")
        private String thirdFromVirtualWarehouseId;

        /**
        * 第三方调入虚拟仓id
        */
        @NotBlank(message = "第三方调入虚拟仓id不能为空")
        @Size(max = 19,message = "第三方调入虚拟仓id最大长度不能超过19位")
        private String thirdToVirtualWarehouseId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
        */
        @NotBlank(message = "类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货不能为空")
        @Size(max = 19,message = "类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货最大长度不能超过19位")
        private String type;

        /**
        * 方向
        */
        @NotNull(message = "方向不能为空")
        private Integer direction;

        /**
        * 系统类型：wdt旺店通
        */
        @NotBlank(message = "系统类型：wdt旺店通不能为空")
        @Size(max = 19,message = "系统类型：wdt旺店通最大长度不能超过19位")
        private String sysType;

        /**
        * 分货单拆单主表id
        */
        @NotBlank(message = "分货单拆单主表id不能为空")
        @Size(max = 19,message = "分货单拆单主表id最大长度不能超过19位")
        private String handleId;


    }

    /**
     * 校验数据DTO
     */
    @Data
    @NoArgsConstructor
    public static class CheckDataDTO {
        /**
         * 单据类型
         */
        private int orderType;
        /**
         * 仓库ID
         */
        private String warehouseId;
        /**
         * 第三方仓库编码
         */
        private String thirdWarehouseNo;
        /**
         * 虚拟仓ID
         */
        private String virtualWarehouseId;
        /**
         * 第三方虚拟仓编码
         */
        private String thirdVirtualWarehouseNo;

        /**
         * SKUId
         */
        private String skuId;
        /**
         * SKU编码
         */
        private String skuNo;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 分货单拆单明细id
         */
        private String handleDetailId;
    }


    /**
     * 三方数据DTO
     */
    @Data
    @NoArgsConstructor
    public static class ThirdDataDTO {
        /**
         * 主表id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 分货单拆单主表id
         */
        private String handleId;
        /**
         * 分货单拆单明细表id
         */
        private String handleDetailId;

        /**
         * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
         */
        private String syncStatus;
        /**
         * 同步平台名称（字符串）
         */
        private String sysType;
        /**
         * 同步平台单号（字符串）
         */
        private String thirdCode;
        /**
         * 完结说明
         */
        private String finishDescription;
    }
}