package com.erp.model.oms.dto;

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
 * <p>
 * 销售价目表明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@NoArgsConstructor
public class SoPriceDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable{

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;


        /**
         * sku id
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

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
    public static class SoTaxPriceSearchDTO {

        /**
         * 销售数量
         */
        @NotNull(message = "销售数量不能为空")
        private Integer soQty;

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
         * 客户id
         */
        private String customerId;

        /**
         * 销售组织id
         */
        private String soOrgId;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SoTaxPriceBatchSearchDTO {


        /**
         * skuid集合
         */
        private List<String> skuIdList;

        /**
         * 客户id集合
         */
        private List<String> customerIdList;

        /**
         * 数量集合
         */
        private List<Integer> soQtyList;

        /**
         * 销售组织id集合
         */
        private List<String> soOrgIdList;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class SoTaxPriceBatchViewDTO extends  SoTaxPriceViewDTO{

        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 销售数量
         */
        private Integer soQty;

        /**
         * 失效时间
         */
        private LocalDate expireDate;

        /**
         * 生效时间
         */
        private LocalDate effectiveDate;
    }

    @Data
    @NoArgsConstructor
    public static class SoTaxPriceViewDTO {



        /**
         * 客户表id
         */
        private String customerId;

        /**
         * 客户表名称
         */
        private String customerName;

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
         * 销售组织
         */
        private String soOrgId;

        /**
         * 销售组织名
         */
        private String soOrgName;
    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {


        /**
         * 成功返回数据
         */
        private List<SoPriceDetailDTO.AddDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 销售价目id
         */
        private String mainId;

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

        /**
         * 价目表编码
         */
        private String priceCode;

        /**
         * 销售组织名称
         */
        private String soOrgName;

        /**
         * 客户表id
         */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;
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
         * 失效时间
         */
        private LocalDate expireDate;


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