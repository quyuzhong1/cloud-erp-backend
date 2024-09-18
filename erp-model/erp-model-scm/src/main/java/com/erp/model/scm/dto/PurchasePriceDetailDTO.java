package com.erp.model.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailDTO

 * @Date 2023-03-16 14:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 供应商表id
         */
        private String supplierId;

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;


        /**
         * sku id
         */
        private String skuNo;


        private String productName;

        /**
         * 采购交期
         */
        private Integer deliveryDay;

        /**
         * 最小数量
         */

        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间到 不能为空")
        private Integer minQty;

        /**
         * 最大数量
         */
        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间到 不能为空")
        private Integer maxQty;

        /**
         * 生效时间
         */
        @NotNull(message = "生效时间不能为空")
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        @NotNull(message = "失效时间不能为空")
        private LocalDate expireDate;


        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        @DecimalMin(value = "0",message ="含税单价必须大于0",inclusive = false )
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        private BigDecimal taxRate;


        /**
         * true 禁用
         * false 启用
         * 默认false
         */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseTaxPriceSearchDTO {

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        private Integer purchaseQty;

        /**
         * sku id
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseTaxPriceBatchSearchDTO {


        /**
         * skuid集合
         */
        private List<String> skuIdList;

        /**
         * 供应商id集合
         */
        private List<String> supplierIdList;

        /**
         * 数量集合
         */
        private List<Integer> purchaseQtyList;

        /**
         * 采购组织id集合
         */
        private List<String> purchaseOrgIdList;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class PurchaseTaxPriceBatchViewDTO extends  PurchaseTaxPriceViewDTO{

        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 采购数量
         */
        private Integer purchaseQty;
    }

        @Data
    @NoArgsConstructor
    public static class PurchaseTaxPriceViewDTO {



        /**
         * 供应商表id
         */
        private String supplierId;

        /**
         * 供应商表名称
         */
        private String supplierName;

        /**
         * 最小数量
         */
        private Integer minQty;

        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 币别
         */
        private String currency;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 采购交期（天）
         */
        private Integer deliveryDay;

        /**
         * 采购组织
         */
        private String purchaseOrgId;

        /**
         * 采购组织名
         */
        private String purchaseOrgName;
    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {


        /**
         * 成功返回数据
         */
        private List<PurchasePriceDetailDTO.AddDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 采购价目id
         */
        private String purchasePriceId;

        /**
         * 主键id
         */
        private String id;

        /**
         * sku id
         */
        private String skuId;


        /**
         * 产品名称
         */
        private String productName;


        /**
         * sku_no
         */
        private String skuNo;


        /**
         * 采购交期
         */
        private Integer deliveryDay;

        /**
         * 最小数量
         */
        private Integer minQty;

        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 币种
         */
        private String currency;


        /**
         * 生效时间
         */
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        private LocalDate expireDate;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

        private String kingdeeDetailId;

        /**
         * 供应商表id
         */
        private String supplierId;

        /**
         * 供应商表名称
         */
        private String supplierName;

        /**
         * 价目表编码
         */
        private String priceCode;

        /**
         * 采购组织名称
         */
        private String purchaseOrgName;
    }


    @Data
    @NoArgsConstructor
    public static class HistoryDTO {


        private String id;

        /**
         * sku id
         */
        private String skuId;


        /**
         * 产品名称
         */
        private String productName;


        /**
         * sku_no
         */
        private String skuNo;


        /**
         * 采购交期
         */
        private Integer deliveryDay;

        /**
         * 最小数量
         */
        private Integer minQty;

        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 币种
         */
        private String currency;


        /**
         * 生效时间
         */
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        private LocalDate expireDate;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        private Boolean disabled;


        private LocalDateTime createTime;


    }


    @Data
    @NoArgsConstructor
    public static class ImportSaveDTO {

        private List<String> ids;

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;


        /**
         * sku id
         */
        @Size(max = 64, message = "SKU编号最大64字符")
        private String skuNo;

        private String productName;

        /**
         * 采购交期
         */
        private Integer deliveryDay;

        /**
         * 最小数量
         */
        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间从 不能为空")
        private Integer minQty;

        /**
         * 最大数量
         */
        @DecimalMax(value = "999999999",message ="最大值为999999999" )
        @DecimalMin(value = "0",message ="最小值为0" )
        @NotNull(message = "区间到 不能为空")
        private Integer maxQty;

        /**
         * 币种
         */
        private String currency;


        /**
         * 生效时间
         */
        private LocalDate effectiveDate;


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
         * true 禁用
         * false 启用
         * 默认false
         */
        private Boolean disabled;

        /**
         * 定价员id
         */
        private String pricingUserId;
    }


}
