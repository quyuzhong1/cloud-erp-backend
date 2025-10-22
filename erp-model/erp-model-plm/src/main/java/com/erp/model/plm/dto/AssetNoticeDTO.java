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
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import org.apache.xpath.operations.Bool;

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
         * 明细id
         */
        private String  detailId;

        /**
        * 单号
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
         * 创建人id
         */
        private String createUserId;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
         * 资产id
         */
        private String assetId;

        /**
         * 资产编码
         */
        private String assetCode;

        /**
         * 资产名称
         */
        private String assetName;

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
         * 计划交期
         */
        private LocalDate planDeliverDate;

        /**
         * 申请数量
         */
        private BigDecimal applyQty;

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
        * 单号
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
         * 资产通知单明细
         */
        private List<AssetNoticeDetailDTO.ViewDTO> assetNoticeDetailDTOList;
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
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

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
        @NotNull(message = "申请日期不能为空")
        private LocalDate applyDate;


    }


    @Data
    @NoArgsConstructor
    public static class ListGeneratePurchaseOrderDTO{

        /**
         * 采购通知单id
         */
        @NotBlank(message = "采购通知单id不能为空")
        private String id;

        /**
         * 资产通知单明细id
         */
        @NotBlank(message = "资产通知单明细id不能为空")
        private String assetNoticeDetailId;

        /**
         * 资产通知单号
         */
        @NotBlank(message = "资产通知单号不能为空")
        private String code;

        /**
         * 资产id
         */
        @NotBlank(message = "资产id不能为空")
        private String assetId;

        /**
         * 资产编码
         */
        @NotBlank(message = "资产编码不能为空")
        private String assetCode;

        /**
         * 资产名称
         */
        @NotBlank(message = "资产名称不能为空")
        private String assetName;


        /**
         * 采购组织id
         */
        @NotBlank(message = "采购组织id不能为空")
        private String purchaseOrgId;

        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织名称不能为空")
        private String purchaseOrgName;

        /**
         * 最小起订量
         */
        private Integer moq;

        /**
         * 采购交期（天）
         */
        private BigDecimal deliveryDay;

        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        @DecimalMin(value = "0.0", inclusive = false, message = "采购数量必须大于0")
        private BigDecimal taxPrice;

        /**
         * 含税金额
         */
        private BigDecimal taxAmount;

        /**
         * 税率
         */
        @NotBlank(message = "税率不能为空")
        private BigDecimal taxRate;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 币种符号
         */
        @NotBlank(message = "币种符号不能为空")
        private String currencySymbol;

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        @DecimalMin(value = "0.0", inclusive = false, message = "采购数量必须大于0")
        private BigDecimal applyQty;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 一级供应商id
         */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;

        /**
         * 一级供应商名称
         */
        @NotBlank(message = "供应商名称不能为空")
        private String supplierName;

        /**
         * 计划交期
         */
        @NotNull(message = "计划交期不能为空")
        private LocalDate planDeliveryDate;

        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class ViewGeneratePurchaseOrderDTO {

        /**
         * 资产通知单id
         */
        private String id;

        /**
         * 资产通知单明细id
         */
        private String assetNoticeDetailId;

        /**
         * 资产通知单号
         */
        private String code;

        /**
         * 资产id
         */
        private String assetId;

        /**
         * 资产编码
         */
        private String assetCode;

        /**
         * 资产名称
         */
        private String assetName;


        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织
         */
        private String purchaseOrgName;

        /**
         * 最小起订量
         */
        private Integer moq;

        /**
         * 采购交期（天）
         */
        private BigDecimal deliveryDay;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 含税金额
         */
        private BigDecimal taxAmount;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 申请数量
         */
        private BigDecimal applyQty;

        /**
         * 待采购数量
         */
        private BigDecimal waitQty;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 一级供应商id
         */
        private String supplierId;

        /**
         * 一级供应商名称
         */
        private String supplierName;

        /**
         * 预计交货日期
         */
        private LocalDate planDeliveryDate;

    }
}