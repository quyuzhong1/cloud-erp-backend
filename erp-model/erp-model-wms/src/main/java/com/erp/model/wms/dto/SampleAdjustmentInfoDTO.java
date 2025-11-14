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

/**
 * <p>
 * 样品调整单请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
*/
@Data
@NoArgsConstructor
public class SampleAdjustmentInfoDTO implements Serializable {


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
         * 类型名称
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
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 调整人ID
        */
        private String adjustmentUserId;

        /**
        * 调整人姓名
        */
        private String adjustmentUserName;

        /**
        * 调整部门ID
        */
        private String adjustmentDeptId;

        /**
        * 调整部门名称
        */
        private String adjustmentDeptName;

        /**
        * 调整日期
        */
        private LocalDate adjustmentDate;

        /**
        * 调整类型
        */
        private String adjustmentType;

        /**
        * 调整类型名称
        */
        private String adjustmentTypeName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废备注
        */
        private String invalidRemark;


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
         * 明细ID
         */
        private String detailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 使用方
         */
        private String userSide;

        /**
         * 台账数量
         */
        private Integer ledgerQty;

        /**
         * 实际数量
         */
        private Integer actualQty;

        /**
         * 差异数量
         */
        private Integer differenceQty;

        /**
         * 明细备注
         */
        private String detailRemark;
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
        * 单据编号
        */
        private String code;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 调整人ID
        */
        private String adjustmentUserId;

        /**
        * 调整人姓名
        */
        private String adjustmentUserName;

        /**
        * 调整部门ID
        */
        private String adjustmentDeptId;

        /**
        * 调整部门名称
        */
        private String adjustmentDeptName;

        /**
        * 调整日期
        */
        private LocalDate adjustmentDate;

        /**
        * 调整类型
        */
        private String adjustmentType;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废备注
        */
        private String invalidRemark;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空" )
        private List<SampleAdjustmentDetailDTO.@Valid AddDTO> detailList;

        /**
         * 附件名称集合
         */
        private List<String> attachmentNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachmentUrlList;

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
         * 明细
         */
        @NotEmpty(message = "明细不能为空" )
        private List<SampleAdjustmentDetailDTO.@Valid UpdateDTO> detailList;

        /**
         * 附件名称集合
         */
        private List<String> attachmentNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachmentUrlList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 调整人ID
        */
        @NotBlank(message = "调整人ID不能为空")
        @Size(max = 19,message = "调整人ID最大长度不能超过19位")
        private String adjustmentUserId;

        /**
        * 调整人姓名
        */
        @Size(max = 32,message = "调整人姓名最大长度不能超过32位")
        private String adjustmentUserName;

        /**
        * 调整部门ID
        */
        @NotBlank(message = "调整部门ID不能为空")
        @Size(max = 19,message = "调整部门ID最大长度不能超过19位")
        private String adjustmentDeptId;

        /**
        * 调整部门名称
        */
        @Size(max = 32,message = "调整部门名称最大长度不能超过32位")
        private String adjustmentDeptName;

        /**
        * 调整日期
        */
        private LocalDate adjustmentDate;

        /**
        * 调整类型
        */
        @NotBlank(message = "调整类型不能为空")
        @Size(max = 20,message = "调整类型最大长度不能超过20位")
        private String adjustmentType;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}