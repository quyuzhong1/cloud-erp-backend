package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname PurchasePriceDTO

 * @Date 2023-03-16 14:54
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceChangeDTO implements Serializable {


    /**
     * 添加采购价目变更
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends PermissionsDTO {

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;

        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 原因
         */
        private String reason;


        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList;


    }


    /**
     * 修改采购价目变更
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  extends PermissionsDTO{



        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 原因
         */
        private String reason;

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;

        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceChangeDetailDTO.UpdateDTO> purchasePriceChangeDetailList;

    }



    /**
     * 修改采购价目详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {



        @NotBlank(message = "id不能为空")
        private String id;


        private String code;


        /**
         * 采购价目表id
         */
        @NotBlank(message = "采购价目表id 不能为空")
        private String purchasePriceId;

        /**
         * 价目表id
         */
        private List<String> purchasePriceIdList;

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;


        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;

        /**
         * 原因
         */
        private String reason;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        private List<PurchasePriceChangeDetailDTO.ViewDTO> purchasePriceChangeDetailList;

    }


    /**
     * 采购价目变更分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 表id
         */
        private String id;


        /**
         * 详情id
         */
        private String changeDetailId;

        /**
         * 价目表id
         */
        private String purchasePriceId;

        /**
         * 价目明细id
         */
        private String purchasePriceDetailId;

        /**
         * code
         */
        private String code;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 最小数量
         */
        private Integer minQty;


        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 单据状态code
         */
        private String approveStatusCode;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 币种
         */
        private String currency;


        /**
         * 币种 符号
         */
        private String currencySymbol;

        /**
         *  禁用启用状态
         *  true 禁用
         *  fase 启用
         */
        private Boolean disabled;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;


        /**
         * 生效时间
         */
        private LocalDate effectiveDate;

        /**
         * 升降比例（带百分比）【不能排序】
         */
        private String offsetRate;


        /**
         * 采购组织
         */
        private String purchaseOrgId;



        /**
         * 采购组织名
         */
        private String purchaseOrgName;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建人名称
         */
        private String createUserName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 备注
         */
        private String detailRemark;

        /**
         *未完单据-已调整
         */
        private Integer adjustedCount;

        /**
         *未完单据-全部统计
         */
        private Integer totalAdjustedCount;
    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO  extends PagingParamDTO{

        /**
         * 主键id集合
         */
        private List<String> ids;
    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 ,(approveIng待我审核,approve已审核,reject不通过)
         */
        private String tabFlag;

        /**
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }




    /**
     * 消息推送详情
     */
    @Data
    @NoArgsConstructor
    public static class NoticeMsgViewDTO {

        /**
         * 表id
         */
        private String id;


        /**
         * 详情id
         */
        private String changeDetailId;

        /**
         * 价目表id
         */
        private String purchasePriceId;

        /**
         * 价目明细id
         */
        private String purchasePriceDetailId;

        /**
         * code
         */
        private String code;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;
        /**
         * 供应商采购员id
         */
        private String purchaseUserId;
        /**
         * 供应商采购员
         */
        private String purchaseUserName;

        /**
         * 审核人Id
         */
        private String approveUserId;
        /**-
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建人id
         */
        private String createUserId;
        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }


     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class UpdateApprovalStatusDTO {
         private PurchasePriceChangeEntity purchasePricechangeEntity;
         private ApproveStatusEnum approveStatus;
     }


    /**
     * 采购订单调整
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderAdjustParamDTO {
        /**
         * 供应商Id
         */
        private String supplierId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 最小数量
         */
        private Integer minQty;
        /**
         * 最大数量
         */
        private Integer maxQty;
    }


    /**
     * 采购订单调整
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderAdjustResultDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;
        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;
        /**
         * 供应商Id
         */
        private String supplierId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 数量
         */
        private Integer purchaseQty;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;
    }
}
