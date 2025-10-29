package com.erp.model.scm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.*;

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
public class AssetPurchaseOrderDetailDTO implements Serializable {


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
        * 资产采购单单头id
        */
        private String mainId;

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
         * 标识(首套模first、复制模copy)
         */
        private String tag;

        /**
         * 标识名称(首套模first、复制模copy)
         */
        private String tagName;

        /**
        * 含税单价
        */
        private BigDecimal taxPrice;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 采购数量
        */
        private BigDecimal purchaseQty;

        /**
        * 价税合计
        */
        private BigDecimal totalAmount;

        /**
        * 计划交期
        */
        private LocalDate planDeliveryDate;

        /**
        * 是否加急
        */
        private Boolean isUrgent;

        /**
        * 备注
        */
        private String remark;

        /**
        * 金蝶明细id
        */
        private String kingdeeDetailId;

        /**
        * 结束收货AssetPurchaseOrderReceiveEnum
        */
        private String endReceive;

        /**
         * 结束收货名称AssetPurchaseOrderReceiveEnum
         */
        private String endReceiveName;

        /**
        * 结束验收时间
        */
        private LocalDateTime endReceiveTime;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
         * 关联SKU详情
         */
        private List<AssetPurchaseOrderDetailDTO.AssetDetailRefSkuDTO> assetDetailRefSkuDTOList;
    }

    /**
     * 关联SKU详情
     */
    @Data
    @NoArgsConstructor
    public static class AssetDetailRefSkuDTO {

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
         * 项目编号
         */
        private String projectCode;

        /**
         * 项目名称
         */
        private String projectName;
        /**
         * skuId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 用量
         */
        private BigDecimal skuQty;
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
        * 资产采购单单头id
        */
        private String mainId;

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
        * 含税单价
        */
        private BigDecimal taxPrice;

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
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 采购数量
        */
        @NotNull(message = "采购数量不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购数量整数位不能超过12位，小数位不能超过4位")
        @DecimalMin(value = "0.0", inclusive = false, message = "采购数量必须大于0")
        private BigDecimal purchaseQty;

        /**
        * 价税合计
        */
        private BigDecimal totalAmount;

        /**
        * 计划交期
        */
        @NotNull(message = "计划交期不能为空")
        private LocalDate planDeliveryDate;

        /**
        * 是否加急
        */
        @NotNull(message = "是否加急不能为空")
        private Boolean isUrgent;

        /**
        * 备注
        */
        private String remark;

        /**
        * 金蝶明细id
        */
        private String kingdeeDetailId;

        /**
        * 结束收货AssetPurchaseOrderReceiveEnum
        */
        private String endReceive;

        /**
        * 结束验收时间
        */
        private LocalDateTime endReceiveTime;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
         * 标识
         */
        @NotNull(message = "标识不能为空")
        private String tag;

    }

    @Data
    @NoArgsConstructor
    public static class MoldImportDTO {
        /**
         * 序号(相同的为一张单)
         */
        private String serialNumber;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源单号
         */
        private String sourceType;

        /**
         * 采购日期
         */
        private LocalDate purchaseDate;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 采购部门id
         */
        private String purchaseDeptId;

        /**
         * 采购部门名称
         */
        private String purchaseDeptName;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织名称
         */
        private String purchaseOrgName;

        /**
         * 供应商
         */
        private AssetPurchaseOrderDetailDTO.SupplierImportDTO supplierImportDTO;

        /**
         * 明细
         */
        private List<AssetPurchaseOrderDetailDTO.MoldDetailImportDTO> moldDetailImportDTOList;

    }

    @Data
    @NoArgsConstructor
    public static class MoldDetailImportDTO {

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 模具id
         */
        private String assetId;

        /**
         * 模具编码
         */
        private String assetCode;

        /**
         * 模具编码
         */
        private String assetName;

        /**
         * 是否加急
         */
        private Boolean isUrgent;

        /**
         * 计划交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 采购数量
         */
        private BigDecimal  purchaseQty;

        /**
         * 备注
         */
        private String  remark;
    }

    @Data
    @NoArgsConstructor
    public static class SupplierImportDTO{

        /**
         * 供应商id
         */
        private String SupplierId;

        /**
         * 供应商名称
         */
        private String SupplierName;

        /**
         * 结算方式
         */
        private String payMethodId;

        /**
         * 结算方式名称
         */
        private String payMethodName;

        /**
         * 供应商联系人id
         */
        private String contactId;

        /**
         * 币种
         */
        private String payCurrency;

        /**
         * 供应商联系人名称
         */
        private String contactName;

        /**
         * 供应商电话
         */
        private String contactTelNumber;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

        /**
         * 账户名称
         */
        private String payee;

        /**
         * 收款银行
         */
        private String bankName;

        /**
         * 银行账号
         */
        private String bankAccount;
    }

    @Data
    @NoArgsConstructor
    public static class ExportPdfDTO {
        /**
         * 物料编码
         */
        private String skuNo;

        /**
         * 名称
         */
        private String declareName;

        /**
         * 型号
         */
        private String declareModel;

        /**
         * 描述
         */
        private String productName;

        /**
         * 数量
         */
        private BigDecimal purchaseQty;

        /**
         * 单位
         */
        private String unitName;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 不含税单价
         */
        private BigDecimal price;
        /**
         * 不含税单价 使用科学计数法展示
         */
        private String priceStr;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;
        /**
         * 含税单价 使用科学计数法展示
         */
        private String taxPriceStr;

        /**
         * 不含税金额
         */
        private BigDecimal notTaxPurchaseAmount;
        /**
         * 不含税金额 使用科学计数法展示
         */
        private String notTaxPurchaseAmountStr;

        /**
         * 含税金额
         */
        private BigDecimal totalAmount;
        /**
         * 含税金额 使用科学计数法展示
         */
        private String totalAmountStr;

        /**
         * 交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class ViewGeneratePurchaseChangeOrderDTO{

        /**
         * 主键id
         */
        private String  id;

        /**
         * 资产采购单单头id
         */
        private String mainId;

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
         * 标识(首套模first、复制模copy)
         */
        private String tag;

        /**
         * 标识名称(首套模first、复制模copy)
         */
        private String tagName;

        /**
         * 采购数量
         */
        private BigDecimal oldPurchaseQty;

        /**
         * 原含税单价
         */
        private BigDecimal oldTaxPrice;

        /**
         * 价税合计
         */
        private BigDecimal totalAmount;

        /**
         * 原税率
         */
        private BigDecimal oldTaxRate;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 备注
         */
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 关联SKU详情
         */
        private List<AssetPurchaseOrderDetailDTO.AssetDetailRefSkuDTO> assetDetailRefSkuDTOList;
    }
}