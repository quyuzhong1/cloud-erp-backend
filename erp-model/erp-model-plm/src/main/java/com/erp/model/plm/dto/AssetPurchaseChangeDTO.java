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
public class AssetPurchaseChangeDTO implements Serializable {


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
        * 资产变更单号
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
        * 变更日期
        */
        private LocalDate changeDate;

        /**
        * 变更人id
        */
        private String changeUserId;

        /**
        * 变更人名称
        */
        private String changeUserName;

        /**
        * 变更部门id
        */
        private String changeDeptId;

        /**
        * 变更部门名称
        */
        private String changeDeptName;

        /**
        * 采购组织id
        */
        private String purchaseOrgId;

        /**
        * 采购组织名称
        */
        private String purchaseOrgName;

        /**
        * 变更类型
        */
        private String orderType;

        /**
        * 变更原因
        */
        private String changeReason;

        /**
        * 来源订单id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源订单号
        */
        private String sourceCode;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private String invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 同步金蝶id
        */
        private String syncKingdeeId;


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
         * 原含税单价
         */
        private BigDecimal oldTaxPrice;

        /**
         * 原采购数量
         */
        private BigDecimal oldPurchaseQty;

        /**
         * 原价税合计
         */
        private BigDecimal oldTotalAmount;

        /**
         * 新含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 新采购数量
         */
        private BigDecimal purchaseQty;

        /**
         * 新价税合计
         */
        private BigDecimal totalAmount;

        /**
         * 备注
         */
        private String remark;

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
        * 资产变更单号
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
        * 变更日期
        */
        private LocalDate changeDate;

        /**
        * 变更人id
        */
        private String changeUserId;

        /**
        * 变更人名称
        */
        private String changeUserName;

        /**
        * 变更部门id
        */
        private String changeDeptId;

        /**
        * 变更部门名称
        */
        private String changeDeptName;

        /**
        * 采购组织id
        */
        private String purchaseOrgId;

        /**
        * 采购组织名称
        */
        private String purchaseOrgName;

        /**
        * 变更类型
        */
        private String orderType;

        /**
        * 变更原因
        */
        private String changeReason;

        /**
        * 来源订单id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源订单号
        */
        private String sourceCode;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private String invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 同步金蝶id
        */
        private String syncKingdeeId;

        /**
         *   供应商信息
         */
        private AssetPurchaseOrderSupplierDTO.ViewDTO assetPurchaseOrderSupplierDTO;

        /**
         * 产品明细
         */
        private AssetPurchaseChangeDetailDTO.ViewDTO assetPurchaseChangeDetailDTOList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 资产变更单明细
         */
        @Valid
        private List<AssetPurchaseChangeDetailDTO.AddDTO> assetPurchaseChangeDetailDTOList;
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
         * 资产变更单明细
         */
        @Valid
        private List<AssetPurchaseChangeDetailDTO.AddDTO> assetPurchaseChangeDetailDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 变更日期
        */
        private LocalDate changeDate;

        /**
        * 变更人id
        */
        private String changeUserId;

        /**
        * 变更人名称
        */
        private String changeUserName;

        /**
        * 变更部门id
        */
        private String changeDeptId;

        /**
        * 变更部门名称
        */
        private String changeDeptName;

        /**
        * 采购组织id
        */
        @NotBlank(message = "采购组织id不能为空")
        @Size(max = 255,message = "采购组织id最大长度不能超过255位")
        private String purchaseOrgId;

        /**
        * 采购组织名称
        */
        @NotBlank(message = "采购组织名称不能为空")
        @Size(max = 255,message = "采购组织名称最大长度不能超过255位")
        private String purchaseOrgName;

        /**
        * 变更类型
        */
        @NotBlank(message = "变更类型不能为空")
        @Size(max = 255,message = "变更类型最大长度不能超过255位")
        private String orderType;

        /**
        * 变更原因
        */
        @NotBlank(message = "变更原因不能为空")
        @Size(max = 255,message = "变更原因最大长度不能超过255位")
        private String changeReason;

        /**
        * 来源订单id
        */
        @NotBlank(message = "来源订单id不能为空")
        @Size(max = 255,message = "来源订单id最大长度不能超过255位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 255,message = "来源类型最大长度不能超过255位")
        private String sourceType;

        /**
        * 来源订单号
        */
        @NotBlank(message = "来源订单号不能为空")
        @Size(max = 255,message = "来源订单号最大长度不能超过255位")
        private String sourceCode;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 同步金蝶id
        */
        private String syncKingdeeId;


    }


}