package com.erp.model.fms.dto;

import java.math.BigDecimal;
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

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 资产验收表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetAcceptDTO implements Serializable {


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
          * 类型名臣
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

        private String approveStatus;

        private String approveUserId;

        private String approveUserName;

        private LocalDateTime approveTime;

        private Boolean invalidStatus;

        private String invalidRemark;

        private LocalDateTime invalidTime;

        /**
        * 验收单号
        */
        private String code;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 验收日期
        */
        private LocalDate acceptDate;

        /**
        * 采购单号
        */
        private String purchaseCode;

        /**
        * 是否需要盖章
        */
        private Boolean isNeedSeal;

        /**
        * 验收组织ID
        */
        private String acceptOrgId;

        /**
        * 验收组织名称
        */
        private String acceptOrgName;

        /**
        * 验收人ID
        */
        private String acceptUserId;

        /**
        * 验收人姓名
        */
        private String acceptUserName;

        /**
        * 验收部门ID
        */
        private String acceptDeptId;

        /**
        * 验收部门名称
        */
        private String acceptDeptName;

        /**
        * 供应商ID
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 验收说明
        */
        private String acceptDesc;


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
         * 验收人员中文名称（多个用逗号分隔）
         */
        private String acceptPersonNames;

        /**
         * 资产卡片关联状态名称
         */
        private String assetCardStatusName;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 验收数量
         */
        private Integer acceptQty;

        /**
         * 资产卡片关联状态
         */
        private String assetCardStatus;

        /**
         * 明细ID
         */
        private String detailId;
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

        private String approveStatus;

        private String approveUserId;

        private String approveUserName;

        private LocalDateTime approveTime;

        private Boolean invalidStatus;

        private String invalidRemark;

        private LocalDateTime invalidTime;

        /**
        * 验收单号
        */
        private String code;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 验收日期
        */
        private LocalDate acceptDate;

        /**
        * 采购单号
        */
        private String purchaseCode;

        /**
        * 是否需要盖章
        */
        private Boolean isNeedSeal;

        /**
        * 验收组织ID
        */
        private String acceptOrgId;

        /**
        * 验收组织名称
        */
        private String acceptOrgName;

        /**
        * 验收人ID
        */
        private String acceptUserId;

        /**
        * 验收人姓名
        */
        private String acceptUserName;

        /**
        * 验收部门ID
        */
        private String acceptDeptId;

        /**
        * 验收部门名称
        */
        private String acceptDeptName;

        /**
        * 供应商ID
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 验收说明
        */
        private String acceptDesc;

        /**
         * 验收人员列表
         */
        private List<AssetAcceptPersonDTO.ViewDTO> personList;

        /**
         * 验收明细列表
         */
        private List<AssetAcceptDetailDTO.ViewDTO> detailList;

        /**
         * 附件URL列表
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称列表
         */
        private List<String> attachmentNameList;

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

        private LocalDateTime invalidTime;

        /**
        * 来源单号
        */
        @Size(max = 100,message = "来源单号最大长度不能超过100位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @Size(max = 200,message = "来源类型最大长度不能超过200位")
        private String sourceType;

        /**
        * 来源ID
        */
        @Size(max = 19,message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
        * 验收日期
        */
        private LocalDate acceptDate;

        /**
        * 采购单号
        */
        @NotBlank(message = "采购单号不能为空")
        @Size(max = 32,message = "采购单号最大长度不能超过32位")
        private String purchaseCode;

        /**
        * 是否需要盖章
        */
        @NotNull(message = "是否需要盖章不能为空")
        private Boolean isNeedSeal;

        /**
        * 验收组织ID
        */
        @NotBlank(message = "验收组织ID不能为空")
        @Size(max = 19,message = "验收组织ID最大长度不能超过19位")
        private String acceptOrgId;

        /**
        * 验收组织名称
        */
        @NotBlank(message = "验收组织名称不能为空")
        @Size(max = 50,message = "验收组织名称最大长度不能超过50位")
        private String acceptOrgName;

        /**
        * 验收人ID
        */
        @NotBlank(message = "验收人ID不能为空")
        @Size(max = 19,message = "验收人ID最大长度不能超过19位")
        private String acceptUserId;

        /**
        * 验收人姓名
        */
        @NotBlank(message = "验收人姓名不能为空")
        @Size(max = 50,message = "验收人姓名最大长度不能超过50位")
        private String acceptUserName;

        /**
        * 验收部门ID
        */
        @NotBlank(message = "验收部门ID不能为空")
        @Size(max = 19,message = "验收部门ID最大长度不能超过19位")
        private String acceptDeptId;

        /**
        * 验收部门名称
        */
        @NotBlank(message = "验收部门名称不能为空")
        @Size(max = 50,message = "验收部门名称最大长度不能超过50位")
        private String acceptDeptName;

        /**
        * 供应商ID
        */
        @NotBlank(message = "供应商ID不能为空")
        @Size(max = 19,message = "供应商ID最大长度不能超过19位")
        private String supplierId;

        /**
        * 供应商名称
        */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 100,message = "供应商名称最大长度不能超过100位")
        private String supplierName;

        /**
        * 验收说明
        */
        @NotBlank(message = "验收说明不能为空")
        @Size(max = 500,message = "验收说明最大长度不能超过500位")
        private String acceptDesc;

        /**
         * 验收人员列表
         */
        private List<AssetAcceptPersonDTO.AddDTO> personList;

        /**
         * 验收明细列表
         */
        private List<AssetAcceptDetailDTO.AddDTO> detailList;

        /**
         * 附件URL列表
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称列表
         */
        private List<String> attachmentNameList;

    }

    /**
     * 添加明细查询参数
     */
    @Data
    @NoArgsConstructor
    public static class AddDetailQueryDTO {

        /**
         * 资产验收单ID（编辑时传入）
         */
        private String assetAcceptId;

        /**
         * 资产采购订单ID（新增时传入，用于确定来源）
         */
        private String assetPurchaseOrderId;

        /**
         * SKU搜索条件（支持多个，用换行分隔）
         */
        private String searchKeyword;

        /**
         * 高级查询条件
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * 当前页码
         */
        private Integer currPage = 1;

        /**
         * 每页大小
         */
        private Integer pageSize = 20;

    }

    /**
     * 添加明细查询结果项
     */
    @Data
    @NoArgsConstructor
    public static class AddDetailItemDTO {

        /**
         * 是否已选中
         */
        private Boolean selected = false;

        /**
         * skuId编码
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
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 待验收数量
         */
        private Integer pendingAcceptQty;

        /**
         * 已验收数量
         */
        private Integer acceptedQty;

        /**
         * 可验收数量
         */
        private Integer availableAcceptQty;

        /**
         * 是否加急
         */
        private Boolean isUrgent;

        /**
         * 备注
         */
        private String remark;

        /**
         * 模具编码（如果来源于模具采购订单）
         */
        private String moldCode;

        /**
         * 模具名称（如果来源于模具采购订单）
         */
        private String moldName;
    }

    /**
     * 添加明细查询结果
     */
    @Data
    @NoArgsConstructor
    public static class AddDetailResultDTO {

        /**
         * 是否为模具采购订单下推
         */
        private Boolean isFromMoldPurchaseOrder = false;

        /**
         * 模具采购订单编号
         */
        private String moldPurchaseOrderCode;

        /**
         * 明细列表
         */
        private List<AddDetailItemDTO> list;
    }

    @Data
    @NoArgsConstructor
    public static class AssetPurchaseOrderRefListDTO {

        /**
         * 资产验收单号
         */
        private String AssetAcceptCode;

        /**
         * 单据状态
         */
        private String approveStatuts;

        /**
         * 单据状态名称
         */
        private String approveStatutsName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 验收日期
         */
        private LocalDate acceptDate;

        /**
         * 验收数量
         */
        private BigDecimal acceptQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 审核人id
         */
        private String approveUserId;

        /**
         * 审核人名称
         */
        private String approveUserName;

    }

}