package com.erp.model.oms.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
         * 发票设置id
         */
        @NotBlank(message = "发票设置id不能为空")
        @Size(max = 19, message = "发票设置id最大长度不能超过19位")
        private String mainId;

        /**
         * 发票设置对应的明细
         */
        List<DetailListDTO> detailDTOList;
    }

    /**
     * 新增或删除
     */
    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {
        /**
         * 发票设置id
         */
        @NotBlank(message = "发票设置id不能为空")
        @Size(max = 19, message = "发票设置id最大长度不能超过19位")
        private String mainId;

        /**
         *
         */
        List<DetailListDTO> detailDTOList;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class DetailListDTO {
        /**
         * 平台name
         */
        private String platformName;

        /**
         * 平台value
         */
        private String platformValue;

        /**
         * 平对对应的明细
         */
        List<CommonDTO> detailDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 平台value
         */
        private String dictPlatform;

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
        /**
         * 是否校验类型
         */
        private Boolean isCheckIe;

        private String dictVerifyType;
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

        /**
         *  店铺是否禁用
         */
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDictPlatformDTO {
        /**
         * dictId
         */
        private String id;
        /**
         * dict.name
         */
        private String name;

        /**
         *  dict.value
         */
        private String value;
    }

    @Data
    @NoArgsConstructor
    public static class ParamsDictPlatformDTO {
        /**
         * key = salesPlatform
         */
        @NotBlank(message = "key不能为空")
        private String key;

        /**
         * 目前限制为速卖通和美克多本土店 names = ["AliExpress","mercadolibre"]，对应为dict中的value
         */
        private List<String> names;
    }

    @Data
    @NoArgsConstructor
    public static class DetailDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 发票设置id
         */
        private String mainId;

        /**
         * 平台value
         **/
        private String dictPlatform;

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
}