package com.erp.model.oms.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 发票设置明细请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
@Data
@NoArgsConstructor
public class CfgInvoiceSettingDetailDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 发票设置id
         */
        private String mainId;

        /**
         * 平台name
         */
        private String platformName;

        /**
         * 平台value
         */
        private String platformValue;


        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 开票规则：amount=按产品全额开票，custom=按（产品全额×自定义百分比）后开票,deduct=按（产品全额-佣金）后开票
         */
        private String dictInvoiceRule;

        /**
         * 比例
         */
        private BigDecimal ratio;

        /**
         * 是否包含运费
         */
        private Boolean isContainShipFee;

        /**
         * 税费类型：purchase_sale=采购经销，self_sale=自产自销
         */
        private String taxType;

        /**
         * 开票节点：after_pull=订单拉取后，after_audit=订单审核后，no_auto=不自动开票
         */
        private String invoiceNode;

        /**
         * 自动上传
         */
        private Boolean isAutoUpload;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        @Size(max = 19, message = "发票设置明细id最大长度不能超过19位")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 发票设置id
         */
        @NotBlank(message = "发票设置id不能为空")
        @Size(max = 19, message = "发票设置id最大长度不能超过19位")
        private String mainId;

        /**
         * 平台name
         */
        @NotBlank(message = "平台name不能为空")
        @Size(max = 50, message = "平台name最大长度不能超过50位")
        private String platformName;

        /**
         * 平台value
         */
        @NotBlank(message = "平台value不能为空")
        @Size(max = 50, message = "平台value最大长度不能超过19位")
        private String platformValue;

        /**
         * 平台value
         */
        private String dictPlatform;

        /**
         * 店铺id
         */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19, message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
         * 开票规则：amount=按产品全额开票，custom=按（产品全额×自定义百分比）后开票,deduct=按（产品全额-佣金）后开票
         */
        @NotBlank(message = "开票规则：amount=按产品全额开票，custom=按（产品全额×自定义百分比）后开票,deduct=按（产品全额不能为空")
        @Size(max = 50, message = "开票规则：amount=按产品全额开票，custom=按（产品全额×自定义百分比）后开票,deduct=按（产品全额最大长度不能超过50位")
        private String dictInvoiceRule;

        /**
         * 比例
         */
        @NotNull(message = "比例不能为空")
        @Digits(integer = 8, fraction = 2, message = "比例整数位不能超过8位，小数位不能超过2位")
        private BigDecimal ratio;

        /**
         * 是否包含运费
         */
        @NotNull(message = "是否包含运费不能为空")
        private Boolean isContainShipFee;

        /**
         * 税费类型：purchase_sale=采购经销，self_sale=自产自销
         */
        @NotBlank(message = "税费类型：purchase_sale=采购经销，self_sale=自产自销不能为空")
        @Size(max = 50, message = "税费类型：purchase_sale=采购经销，self_sale=自产自销最大长度不能超过50位")
        private String taxType;

        /**
         * 开票节点：after_pull=订单拉取后，after_audit=订单审核后，no_auto=不自动开票
         */
        @NotBlank(message = "开票节点：after_pull=订单拉取后，after_audit=订单审核后，no_auto=不自动开票不能为空")
        @Size(max = 50, message = "开票节点：after_pull=订单拉取后，after_audit=订单审核后，no_auto=不自动开票最大长度不能超过50位")
        private String invoiceNode;

        /**
         * 自动上传
         */
        @NotNull(message = "自动上传不能为空")
        private Boolean isAutoUpload;

    }

    /**
     * 绑定店铺
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetailShop {
        /**
         * 发票设置id
         */
        private String mainId;
        /**
         * 平台value
         */
        private String dictPlatform;
        /**
         * 平台店铺name
         */
        private String name;
    }

    @Data
    @NoArgsConstructor
    public static class ViewParamsDTO {
        /**
         * main_id
         */
        @NotBlank(message = "id不能为空")
        @Size(max = 19, message = "id最大长度不能超过19位")
        private String id;
        /**
         * key = salesPlatform
         */
        @NotBlank(message = "key不能为空")
        private String key;

        /**
         * 目前限制为速卖通和美克多本土店 names = ["AliExpress","mercadolibre"]
         */
        private List<String> names;
    }

    @Data
    @NoArgsConstructor
    public static class ViewShopDTO {
        /**
         * shopid
         */
        private String id;
        /**
         * shop.dictplatform
         */
        private String name;

        /**
         *  shop.name
         */
        private String value;
    }
}