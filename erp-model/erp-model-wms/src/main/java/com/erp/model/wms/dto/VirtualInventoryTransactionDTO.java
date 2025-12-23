package com.erp.model.wms.dto;

import java.time.LocalDate;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 虚拟仓库存事务表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-12-18
*/
@Data
@NoArgsConstructor
public class VirtualInventoryTransactionDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

     }


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

     }


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

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
        * 虚拟仓id
        */
        private String virtualWarehouseId;

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

        /**
        * 操作类型
        */
        private String operationMode;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 虚拟仓id
        */
        private String virtualWarehouseId;

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

        /**
        * 操作类型
        */
        private String operationMode;


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
    public static class CommonDTO extends SuperDTO {

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
        * 虚拟仓id
        */
        @NotBlank(message = "虚拟仓id不能为空")
        @Size(max = 32,message = "虚拟仓id最大长度不能超过32位")
        private String virtualWarehouseId;

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

        /**
        * 操作类型
        */
        @NotBlank(message = "操作类型不能为空")
        @Size(max = 50,message = "操作类型最大长度不能超过50位")
        private String operationMode;


    }


}