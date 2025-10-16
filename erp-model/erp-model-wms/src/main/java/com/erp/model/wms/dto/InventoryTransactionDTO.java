package com.erp.model.wms.dto;

import java.time.LocalDate;
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
 * 库存事务表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-10-13
*/
@Data
@NoArgsConstructor
public class InventoryTransactionDTO implements Serializable {




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
        * 库存表id
        */
        private String inventoryId;

        /**
        * 组织id
        */
        private String orgId;

        /**
        * 仓库id 
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 仓位id
        */
        private String warehouseLocation;

        /**
        * 库存状态
        */
        private String dictInventoryStatus;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * sku id
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 库存流水id
        */
        private String flowId;

        /**
        * 事务id
        */
        private String transactionId;

        /**
        * 事务类型，global全局事务，local本地事务
        */
        private String transactionType;


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
        * 库存表id
        */
        @NotBlank(message = "库存表id不能为空")
        @Size(max = 19,message = "库存表id最大长度不能超过19位")
        private String inventoryId;

        /**
        * 组织id
        */
        @NotBlank(message = "组织id不能为空")
        @Size(max = 19,message = "组织id最大长度不能超过19位")
        private String orgId;

        /**
        * 仓库id 
        */
        @NotBlank(message = "仓库id 不能为空")
        @Size(max = 19,message = "仓库id 最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 200,message = "仓库名称最大长度不能超过200位")
        private String warehouseName;

        /**
        * 仓位id
        */
        @NotBlank(message = "仓位id不能为空")
        @Size(max = 32,message = "仓位id最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 库存状态
        */
        @NotBlank(message = "库存状态不能为空")
        @Size(max = 32,message = "库存状态最大长度不能超过32位")
        private String dictInventoryStatus;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 库存流水id
        */
        @NotBlank(message = "库存流水id不能为空")
        @Size(max = 19,message = "库存流水id最大长度不能超过19位")
        private String flowId;

        /**
        * 事务id
        */
        @NotBlank(message = "事务id不能为空")
        @Size(max = 64,message = "事务id最大长度不能超过64位")
        private String transactionId;

        /**
        * 事务类型，global全局事务，local本地事务
        */
        @NotBlank(message = "事务类型，global全局事务，local本地事务不能为空")
        @Size(max = 64,message = "事务类型，global全局事务，local本地事务最大长度不能超过64位")
        private String transactionType;


    }


}