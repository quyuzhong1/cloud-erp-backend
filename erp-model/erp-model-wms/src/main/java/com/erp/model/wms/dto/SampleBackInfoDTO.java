package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 样品退回单请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@NoArgsConstructor
public class SampleBackInfoDTO implements Serializable {


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
        * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回)
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态(false:有效,true:已作废)
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 样品退回单号
        */
        private String code;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 执行状态
        */
        private String execStatus;

        private LocalDate backDate;

        /**
        * 来源ID（关联样品领用单）
        */
        private String sourceId;

        /**
        * 来源单号（样品领用单号）
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 退回人ID
        */
        private String userId;

        /**
        * 退回人姓名
        */
        private String userName;

        /**
        * 退回部门ID
        */
        private String deptId;

        /**
        * 收货仓库ID
        */
        private String warehouseId;

        private String warehouseName;

        /**
        * 退回组织ID
        */
        private String orgId;

        /**
        * 备注
        */
        private String remark;


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
        * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回)
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态(false:有效,true:已作废)
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 样品退回单号
        */
        private String code;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 执行状态
        */
        private String execStatus;

        private LocalDate backDate;

        /**
        * 来源ID（关联样品领用单）
        */
        private String sourceId;

        /**
        * 来源单号（样品领用单号）
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 退回人ID
        */
        private String userId;

        /**
        * 退回人姓名
        */
        private String userName;

        /**
        * 退回部门ID
        */
        private String deptId;

        /**
        * 收货仓库ID
        */
        private String warehouseId;

        private String warehouseName;

        /**
        * 退回组织ID
        */
        private String orgId;

        /**
        * 备注
        */
        private String remark;

        /**
        * 明细列表
        */
        private List<SampleBackDetailDTO.ViewDTO> detailList;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 明细列表
         */
        @NotEmpty(message = "明细不能为空")
        private List<SampleBackDetailDTO.@Valid AddDTO> detailList;

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

        /**
         * 明细列表
         */
        @NotEmpty(message = "明细不能为空")
        private List<SampleBackDetailDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 执行状态
        */
        @NotBlank(message = "执行状态不能为空")
        @Size(max = 50,message = "执行状态最大长度不能超过50位")
        private String execStatus;

        private LocalDate backDate;

        /**
        * 来源ID（关联样品领用单）
        */
        @NotBlank(message = "来源ID（关联样品领用单）不能为空")
        @Size(max = 19,message = "来源ID（关联样品领用单）最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源单号（样品领用单号）
        */
        @NotBlank(message = "来源单号（样品领用单号）不能为空")
        @Size(max = 32,message = "来源单号（样品领用单号）最大长度不能超过32位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 50,message = "来源类型最大长度不能超过50位")
        private String sourceType;

        /**
        * 退回人ID
        */
        @NotBlank(message = "退回人ID不能为空")
        @Size(max = 19,message = "退回人ID最大长度不能超过19位")
        private String userId;

        /**
        * 退回人姓名
        */
        @NotBlank(message = "退回人姓名不能为空")
        @Size(max = 50,message = "退回人姓名最大长度不能超过50位")
        private String userName;

        /**
        * 退回部门ID
        */
        @NotBlank(message = "退回部门ID不能为空")
        @Size(max = 19,message = "退回部门ID最大长度不能超过19位")
        private String deptId;

        /**
        * 收货仓库ID
        */
        @NotBlank(message = "收货仓库ID不能为空")
        @Size(max = 19,message = "收货仓库ID最大长度不能超过19位")
        private String warehouseId;

        @NotBlank(message = "warehouseName不能为空")
        @Size(max = 100,message = "warehouseName最大长度不能超过100位")
        private String warehouseName;

        /**
        * 退回组织ID
        */
        @NotBlank(message = "退回组织ID不能为空")
        @Size(max = 19,message = "退回组织ID最大长度不能超过19位")
        private String orgId;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}