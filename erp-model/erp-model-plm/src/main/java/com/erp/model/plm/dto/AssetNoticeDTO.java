package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@NoArgsConstructor
public class AssetNoticeDTO implements Serializable {


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
        * 项目编号
        */
        private String code;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 单据状态
        */
        private String approveStatus;

        /**
        * 申请人id
        */
        private String applyUserId;

        /**
        * 申请人名称
        */
        private String applyUserName;

        /**
        * 申请部门id
        */
        private String applyDeptId;

        /**
        * 申请部门名称
        */
        private String applyDeptName;

        /**
        * 作废原因
        */
        private String invalidReason;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废日期
        */
        private LocalDate invalidDate;

        /**
        * 申请日期
        */
        private LocalDate applyDate;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;


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

        /**
         * 采购单关联状态
         */
        private String createPoType;

        /**
         * 采购单关联状态（0未生成，1部分生成，2已生成)
         */
        private String createPoTypeName;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 待采购数量
         */
        private BigDecimal waitQty;

        /**
         * 实际采购数量
         */
        private BigDecimal realPurchaseQty;

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
        * 项目编号
        */
        private String code;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 单据状态
        */
        private String approveStatus;

        /**
        * 申请人id
        */
        private String applyUserId;

        /**
        * 申请人名称
        */
        private String applyUserName;

        /**
        * 申请部门id
        */
        private String applyDeptId;

        /**
        * 申请部门名称
        */
        private String applyDeptName;

        /**
        * 作废原因
        */
        private String invalidReason;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废日期
        */
        private LocalDate invalidDate;

        /**
        * 申请日期
        */
        private LocalDate applyDate;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 资产通知单明细
         */
        @Valid
        @NotEmpty(message = "资产通知单明细信息不能为空")
        private List<AssetNoticeDetailDTO.AddDTO> assetNoticeDetailDTO;
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
         * 资产通知单明细
         */
        @Valid
        @NotEmpty(message = "资产通知单明细信息不能为空")
        private List<AssetNoticeDetailDTO.UpdateDTO> assetNoticeDetailDTO;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 申请人id
        */
        private String applyUserId;

        /**
        * 申请人名称
        */
        private String applyUserName;

        /**
        * 申请部门id
        */
        private String applyDeptId;

        /**
        * 申请部门名称
        */
        private String applyDeptName;

        /**
        * 作废原因
        */
        private String invalidReason;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废日期
        */
        private LocalDate invalidDate;

        /**
        * 申请日期
        */
        private LocalDate applyDate;


    }


}