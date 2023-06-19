package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 采购订单表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-19
*/
@Data
@NoArgsConstructor
public class PurchaseOrderDTO implements Serializable {


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
         private String searchType;

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
         * 搜索类型
         */
         private String  searchType;

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
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 采购日期
        */
        private LocalDate purchaseDate;

        /**
        * 采购员id
        */
        private String purchaseUserId;

        /**
        * 采购员名称
        */
        private String purchaseUserName;

        /**
        * 采购组织id
        */
        private String purchaseOrgId;

        /**
        * 采购组织名称
        */
        private String purchaseOrgName;

        /**
        * 采购部门id
        */
        private String purchaseDeptId;

        /**
        * 采购部门名称
        */
        private String purchaseDeptName;

        /**
        * 新品首批（false否,true是）
        */
        private Boolean isFirstMassProduct;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 交货仓库id
        */
        private String deliveryWarehouseId;

        /**
        * 交货仓库名称
        */
        private String deliveryWarehouseName;

        /**
        * 收料组织id
        */
        private String receiveOrgId;

        /**
        * 收料组织名称
        */
        private String receiveOrgName;

        /**
        * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        */
        private String syncKingdeeStatus;

        /**
        * 同步时间
        */
        private LocalDateTime syncKingdeeTime;

        /**
        * 金蝶数据id
        */
        private String syncKingdeeId;

        /**
        * 同步操作
        */
        private String syncOperate;

        /**
        * 来源主键id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 委外订单类型（child子级，parent父级）
        */
        private String subcontractType;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

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
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 采购日期
        */
        private LocalDate purchaseDate;

        /**
        * 采购员id
        */
        private String purchaseUserId;

        /**
        * 采购员名称
        */
        private String purchaseUserName;

        /**
        * 采购组织id
        */
        private String purchaseOrgId;

        /**
        * 采购组织名称
        */
        private String purchaseOrgName;

        /**
        * 采购部门id
        */
        private String purchaseDeptId;

        /**
        * 采购部门名称
        */
        private String purchaseDeptName;

        /**
        * 新品首批（false否,true是）
        */
        private Boolean isFirstMassProduct;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 交货仓库id
        */
        private String deliveryWarehouseId;

        /**
        * 交货仓库名称
        */
        private String deliveryWarehouseName;

        /**
        * 收料组织id
        */
        private String receiveOrgId;

        /**
        * 收料组织名称
        */
        private String receiveOrgName;

        /**
        * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        */
        private String syncKingdeeStatus;

        /**
        * 同步时间
        */
        private LocalDateTime syncKingdeeTime;

        /**
        * 金蝶数据id
        */
        private String syncKingdeeId;

        /**
        * 同步操作
        */
        private String syncOperate;

        /**
        * 来源主键id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 委外订单类型（child子级，parent父级）
        */
        private String subcontractType;


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
        * 采购日期
        */
        private LocalDate purchaseDate;

        /**
        * 采购员id
        */
        @NotBlank(message = "采购员id不能为空")
        @Size(max = 19,message = "采购员id最大长度不能超过19位")
        private String purchaseUserId;

        /**
        * 采购员名称
        */
        @NotBlank(message = "采购员名称不能为空")
        @Size(max = 64,message = "采购员名称最大长度不能超过64位")
        private String purchaseUserName;

        /**
        * 采购组织id
        */
        @NotBlank(message = "采购组织id不能为空")
        @Size(max = 19,message = "采购组织id最大长度不能超过19位")
        private String purchaseOrgId;

        /**
        * 采购组织名称
        */
        @NotBlank(message = "采购组织名称不能为空")
        @Size(max = 100,message = "采购组织名称最大长度不能超过100位")
        private String purchaseOrgName;

        /**
        * 采购部门id
        */
        @NotBlank(message = "采购部门id不能为空")
        @Size(max = 19,message = "采购部门id最大长度不能超过19位")
        private String purchaseDeptId;

        /**
        * 采购部门名称
        */
        @NotBlank(message = "采购部门名称不能为空")
        @Size(max = 64,message = "采购部门名称最大长度不能超过64位")
        private String purchaseDeptName;

        /**
        * 新品首批（false否,true是）
        */
        @NotNull(message = "新品首批（false否,true是）不能为空")
        private Boolean isFirstMassProduct;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 交货仓库id
        */
        @NotBlank(message = "交货仓库id不能为空")
        @Size(max = 19,message = "交货仓库id最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 交货仓库名称
        */
        @NotBlank(message = "交货仓库名称不能为空")
        @Size(max = 200,message = "交货仓库名称最大长度不能超过200位")
        private String deliveryWarehouseName;

        /**
        * 收料组织id
        */
        @NotBlank(message = "收料组织id不能为空")
        @Size(max = 19,message = "收料组织id最大长度不能超过19位")
        private String receiveOrgId;

        /**
        * 收料组织名称
        */
        @NotBlank(message = "收料组织名称不能为空")
        @Size(max = 100,message = "收料组织名称最大长度不能超过100位")
        private String receiveOrgName;

        /**
        * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        */
        @NotBlank(message = "同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）不能为空")
        @Size(max = 1,message = "同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）最大长度不能超过1位")
        private String syncKingdeeStatus;

        /**
        * 同步时间
        */
        private LocalDateTime syncKingdeeTime;

        /**
        * 金蝶数据id
        */
        @NotBlank(message = "金蝶数据id不能为空")
        @Size(max = 100,message = "金蝶数据id最大长度不能超过100位")
        private String syncKingdeeId;

        /**
        * 同步操作
        */
        @NotBlank(message = "同步操作不能为空")
        @Size(max = 32,message = "同步操作最大长度不能超过32位")
        private String syncOperate;

        /**
        * 来源主键id
        */
        @NotBlank(message = "来源主键id不能为空")
        @Size(max = 19,message = "来源主键id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 委外订单类型（child子级，parent父级）
        */
        @NotBlank(message = "委外订单类型（child子级，parent父级）不能为空")
        @Size(max = 32,message = "委外订单类型（child子级，parent父级）最大长度不能超过32位")
        private String subcontractType;


    }


}