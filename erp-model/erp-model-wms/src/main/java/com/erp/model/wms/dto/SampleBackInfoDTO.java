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
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.Valid;

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
          * 名称
          */
         private String tabFlagName;

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
        * 退回组织名称
        */
        private String orgName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 明细ID
        */
        private String detailId;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 退回数量
        */
        private Integer qty;

        /**
        * 明细备注
        */
        private String detailRemark;

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
        /**
         * 附件名称集合
         */
        private List<String> attachNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachUrlList;


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
        * 退回日期
        */
        @NotNull(message = "退回日期不能为空")
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
        @NotBlank(message = "退回人ID不能为空")
        private String userId;

        /**
        * 退回人姓名
        */
        private String userName;

        /**
        * 退回部门ID
        */
        @NotBlank(message = "退回部门ID不能为空")
        private String deptId;

        /**
        * 收货仓库ID
        */
        @NotBlank(message = "收货仓库ID不能为空")
        private String warehouseId;

        private String warehouseName;

        /**
        * 退回组织ID
        */
        @NotBlank(message = "退回组织ID不能为空")
        private String orgId;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
         * 附件名称集合
         */
        private List<String> attachNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachUrlList;

    }

    /**
     * 样品退回单导入DTO
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 样品领用单号
         */
        private String sourceCode;
        
        /**
         * 退回日期
         */
        private LocalDate backDate;
        
        /**
         * 仓库ID
         */
        private String warehouseId;
        
        /**
         * 部门ID
         */
        private String deptId;
        
        /**
         * 备注
         */
        private String remark;
        
        /**
         * 明细列表
         */
        private List<ImportDetailDTO> detailList;
    }

    /**
     * 样品退回单导入明细DTO
     */
    @Data
    @NoArgsConstructor
    public static class ImportDetailDTO {
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * SKU编码
         */
        private String skuNo;
        
        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * 退回数量
         */
        private Integer qty;
        
        /**
         * 备注
         */
        private String remark;
    }


}