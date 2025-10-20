package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.entity.PurchasePriceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class PurchasePriceDTO implements Serializable {

    /**
     * 下推采购订单View
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class PushDownPurchaseView {
        /**
         * id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        private String approveStatus;
        /**
         * detailId
         */
        @NotBlank(message = "detailId不能为空")
        private String detailId;
        /**
         * 供应商联系人表id
         */
        private String supplierContactId;

        /**
         * 采购日期
         */
        private LocalDate purchaseDate;
        /**
         * 采购员id
         */
        private String purchaseUserId;
        /**
         * purchaseId
         */
        private String purchaseId;
        /**
         * 单号
         */
        @NotBlank(message = "单号不能为空")
        private String code;
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;

        /**
         * 采购组织名
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgName;
        /**
         * 交货仓库
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseId;
        /**
         * 交货仓库名称
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseName;
        /**
         * 收料组织
         */
        @NotBlank(message = "收料组织不能为空")
        private String receiveOrgId;
        /**
         * 收料组织名称
         */
        @NotBlank(message = "收料组织不能为空")
        private String receiveOrgName;

        /**
         * sku
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;
        /**
         * 币别
         */
        private String currency;

        private String warehouseLocation;
        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * skuNo
         */
        @NotBlank(message = "skuNo不能为空")
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 退货方式
         */
        private String returnMode;
        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        private BigDecimal taxPrice;
        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        private BigDecimal taxRate;

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        @Min(value = 0,message = "采购数量最小值为0")
        @Max(value = 999999999,message = "采购数量最大值为999999999")
        private Integer purchaseQty;

        private Integer replenishQty;

        private Integer oldPurchaseQty;

        private Integer returnQty;
        /**
         * 价税合计
         */
        @NotNull(message = "价税合计不能为空")
        private BigDecimal totalTaxAmount;

        /**
         * 预计交货日期
         */
        @NotNull(message = "预计交货日期不能为空")
        private LocalDate planDeliveryDate;
        /**
         * 是否赠品：true/false
         */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift = false;
        /**
         * 是否加急（false否，true是）
         */
        @NotNull(message = "是否加急不能为空")
        private Boolean isUrgent = false;
        /**
         * 新品首批
         */
        private String firstMassProduct;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO  extends PermissionsDTO {
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;
        /**
         * 外部平台单号
         */
        private String voucherNo;
        /**
         * 报价人id
         */
        private String pricingUserId;


        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 备注
         */
        private String remark;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 表id
         */
        private String id;

        /**
         * code
         */
        private String code;
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;
        /**
         * 外部平台单号
         */
        private String voucherNo;
        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 报价人id
         */
        private String pricingUserId;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称
         */
        private List<String> attachmentNameList;

        /**
         * 供应商联系人名称,/api/scm/supplier/getSupplierInfo?supplierId=
         */
        private String supplierContactName;

        /**
         * 供应商联系人电话
         */
        private String contactTelNumber;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

        /**
         * 结算币种
         */
        private String currency;

        /**
         * 备注
         */
        private String remark;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceDetailDTO.ViewDTO> purchasePriceDetailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO   extends PermissionsDTO{

        /**
         * 表id
         */
        @NotBlank(message = "采购价目表id不能为空")
        private String id;

        /**
         * code
         */
        private String code;
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;
        /**
         * 外部平台单号
         */
        private String voucherNo;
        /**
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;


        /**
         * 报价人id
         */
        private String pricingUserId;


        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称
         */
        private List<String> attachmentNameList;

        /**
         * 备注
         */
        private String remark;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList;

    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 表id
         * 对应
         * purchasePriceId
         */
        private String id;


        /**
         * 采购价目详情id
         */
        private String purchasePriceDetailId;


        /**
         * code
         */
        private String code;

        /**
         * 外部平台单号
         */
        private String voucherNo;
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
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态code
         */
        private String approveStatusCode;


        /**
         * 单据状态名
         */
        private String approveStatusName;


        /**
         * 最大数量
         */
        private Integer maxQty;


        /**
         * 币种
         */
        private String currency;


        /**
         * 币种 符号
         */
        private String currencySymbol;

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
         * 失效时间
         */
        private LocalDate expireDate;

        /**
         * 采购组织
         */
        private String purchaseOrgId;

        /**
         * 采购组织名
         */
        private String purchaseOrgName;

        /**
         * 最新审核人名称
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
         *  禁用启用状态
         *  true 禁用
         *  fase 启用
         */
        private Boolean disabled;

        /**
         * 明细备注
         */
        private String detailRemark;

        /**
         * 采购交期
         */
        private String deliveryDay;
    }


    /**
     * 分页参数
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
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 ,(approveIng待我审核,reject不通过,approveEnable已审核启用,approveDisabled已审核停用)
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
     * 导出采购价目
     */
    @Data
    @NoArgsConstructor
    public static class SupplierSkuPrice {
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 采购组织Id
         */
        private String purchaseOrgId;
        /**
         * 采购组织名称
         */
        private String purchaseOrgName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 含税单价
         */
        private BigDecimal taxPrice;
        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 最小数量
         */
        private Integer minQty;

        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 币制
         */
        private String currency;

        /**
         * 币制符号
         */
        private String currencySymbol;
    }




    /**
     * 导入DTO
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class ImportAddDTO {

        /**
         * 采购价目主表id（更新时使用）
         */
        private String id;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 50, message = "供应商名称最大50字符")
        private String supplierName;

        /**
         * 报价日期
         */
        private LocalDate quotedDate;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织
         */
        @Size(max = 50, message = "采购组织名称最大50字符")
        private String purchaseOrgName;

        /**
         * 定价员id
         */
        private String pricingUserId;

        /**
         * 定价员
         */
        @Size(max = 50, message = "定价员名称最大50字符")
        private String pricingUserName;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 明细信息
         */
        @Valid
        private List<PurchasePriceDetailDTO.ImportSaveDTO> detailList;



    }

    @Data
    @NoArgsConstructor
    public static class OutPlatformCodeDTO extends BaseIdsDTO.IdsDTO {

        @NotBlank(message = "外部平台单号")
        @Size(max = 255,message = "填写信息不能超过255字符")
        private String voucherNo;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDTO {
        /**
         * 采购组织
         */
        private String purchaseOrgId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 采购数量
         */
        private Integer qty;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 价税合计
         */
        private String amount;
        /**
         * 币种
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 采购交期（天）
         */
        private Integer deliveryDay;

        public PriceDTO(Integer purchaseQty, String skuId, String supplierId, String purchaseOrgId) {
            this.qty = purchaseQty;
            this.skuId = skuId;
            this.supplierId = supplierId;
            this.purchaseOrgId = purchaseOrgId;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateApprovalStatusDTO {
        private PurchasePriceEntity  purchasePriceEntity;
        private ApproveStatusEnum approveStatus;
    }
}
