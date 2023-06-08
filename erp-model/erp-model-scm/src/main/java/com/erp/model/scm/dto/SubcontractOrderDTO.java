package com.erp.model.scm.dto;

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
 * 委外订单请求响应实体
 * </p>
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@NoArgsConstructor
public class SubcontractOrderDTO implements Serializable {


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
        * 单据日期
        */
        private LocalDate billDate;
        /**
        * 收料组织id
        */
        private String receiveOrgId;
        /**
        * 收料组织名称
        */
        private String receiveOrgName;
        /**
        * 采购组织id
        */
        private String purchaseOrgId;
        /**
        * 采购组织名称
        */
        private String purchaseOrgName;
        /**
        * 采购员id
        */
        private String purchaserId;
        /**
        * 采购员名称
        */
        private String purchaserName;
        /**
        * 采购部门id
        */
        private String deptId;
        /**
        * 采购部门名称
        */
        private String deptName;
        /**
        * 委外组织id
        */
        private String subcontractOrgId;
        /**
        * 委外组织名称
        */
        private String subcontractOrgName;
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
        * 来源id
        */
        private String sourceId;
        /**
        * 来源类型
        */
        private String sourceType;
        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 审核状态名称
        */
        private String approveStatusName;
        /**
        * 作废状态名称
        */
        private String invalidStatusName;
        /**
        * sku id
        */
        private String skuId;
        /**
        * 产品名称
        */
        private String productName;
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
        * 单据日期
        */
        private LocalDate billDate;
        /**
        * 收料组织id
        */
        private String receiveOrgId;
        /**
        * 收料组织名称
        */
        private String receiveOrgName;
        /**
        * 采购组织id
        */
        private String purchaseOrgId;
        /**
        * 采购组织名称
        */
        private String purchaseOrgName;
        /**
        * 采购员id
        */
        private String purchaserId;
        /**
        * 采购员名称
        */
        private String purchaserName;
        /**
        * 采购部门id
        */
        private String deptId;
        /**
        * 采购部门名称
        */
        private String deptName;
        /**
        * 委外组织id
        */
        private String subcontractOrgId;
        /**
        * 委外组织名称
        */
        private String subcontractOrgName;
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
        * 来源id
        */
        private String sourceId;
        /**
        * 来源类型
        */
        private String sourceType;
        /**
        * 来源编码
        */
        private String sourceCode;

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
        * 单据日期
        */
        private LocalDate billDate;
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
        * 采购员id
        */
        @NotBlank(message = "采购员id不能为空")
        @Size(max = 19,message = "采购员id最大长度不能超过19位")
        private String purchaserId;
        /**
        * 采购员名称
        */
        @NotBlank(message = "采购员名称不能为空")
        @Size(max = 64,message = "采购员名称最大长度不能超过64位")
        private String purchaserName;
        /**
        * 采购部门id
        */
        @NotBlank(message = "采购部门id不能为空")
        @Size(max = 19,message = "采购部门id最大长度不能超过19位")
        private String deptId;
        /**
        * 采购部门名称
        */
        @NotBlank(message = "采购部门名称不能为空")
        @Size(max = 64,message = "采购部门名称最大长度不能超过64位")
        private String deptName;
        /**
        * 委外组织id
        */
        @NotBlank(message = "委外组织id不能为空")
        @Size(max = 19,message = "委外组织id最大长度不能超过19位")
        private String subcontractOrgId;
        /**
        * 委外组织名称
        */
        @NotBlank(message = "委外组织名称不能为空")
        @Size(max = 100,message = "委外组织名称最大长度不能超过100位")
        private String subcontractOrgName;
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
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;
        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;
        /**
        * 来源编码
        */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 50,message = "来源编码最大长度不能超过50位")
        private String sourceCode;

    }


}