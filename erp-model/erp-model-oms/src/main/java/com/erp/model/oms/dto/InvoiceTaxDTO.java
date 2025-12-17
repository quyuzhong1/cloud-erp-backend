package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 发票税务信息请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-04-07
*/
@Data
@NoArgsConstructor
public class InvoiceTaxDTO implements Serializable {

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class CheckGenerateInvoiceDTO{
        /**
         * 是否已生成税务信息,false则需要补全
         */
        private Boolean isGenerateInvoiceTax;

        /**
         * 主键id
         */
        private String  id;
        /**
         * 店铺id
         */
        private String  shopId;
        /**
         * 销售订单id
         */
        private String  soId;
        /**
         * 店铺名称
         */
        private String  shopName;
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台SKU
         */
        private String platformSkuNo;

        /**
         * 平台产品名称
         */
        private String platformSkuName;

        /**
         * listing表id
         */
        private String listingId;

        /**
         * 发票海关编码
         */
        private String invoiceHsCode;

        /**
         * 单位
         */
        private String unit;

        /**
         * 跨州税务编码
         */
        private String diffStateTaxCode;

        /**
         * 同州税务编码
         */
        private String sameStateTaxCode;

        /**
         * 原产地
         */
        private String dictOrigin;

        /**
         * 开票产品名称
         */
        private String invoiceProductName;

        /**
         * 类型，（invoiceType字典）
         */
        private String type;
        /**
         * 发票地址
         */
        private String invoiceAddress;
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
         * 店铺id
         */
        private String  shopId;
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台SKU
         */
        private String platformSkuNo;

        /**
         * 平台产品名称
         */
        private String platformSkuName;

        /**
        * listing表id
        */
        private String listingId;

        /**
        * 发票海关编码
        */
        private String invoiceHsCode;

        /**
        * 单位
        */
        private String unit;

        /**
        * 跨州税务编码
        */
        private String diffStateTaxCode;

        /**
        * 同州税务编码
        */
        private String sameStateTaxCode;

        /**
        * 原产地
        */
        private String dictOrigin;

        /**
        * 开票产品名称
        */
        private String invoiceProductName;

        /**
        * 类型，（invoiceType字典）
        */
        private String type;

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
        private String id;
        /**
         * 销售订单id
         */
        private String soId;
        /**
         * 发票地址
         */
        private String invoiceAddress;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 店铺Id
         */
        private String shopId;
        /**
         * 平台
         */
        private String platform;

        /**
         * 平台SKU
         */
        private String platformSkuNo;

        /**
        * listing表id
        */
        private String listingId;

        /**
        * 发票海关编码
        */
        @NotBlank(message = "发票海关编码不能为空")
        @Size(max = 32,message = "发票海关编码最大长度不能超过32位")
        private String invoiceHsCode;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 32,message = "单位最大长度不能超过32位")
        private String unit;

        /**
        * 跨州税务编码
        */
        @NotBlank(message = "跨州税务编码不能为空")
        @Size(max = 32,message = "跨州税务编码最大长度不能超过32位")
        private String diffStateTaxCode;

        /**
        * 同州税务编码
        */
        @NotBlank(message = "同州税务编码不能为空")
        @Size(max = 32,message = "同州税务编码最大长度不能超过32位")
        private String sameStateTaxCode;

        /**
        * 原产地
        */
        @NotBlank(message = "原产地不能为空")
        @Size(max = 32,message = "原产地最大长度不能超过32位")
        private String dictOrigin;

        /**
        * 开票产品名称
        */
        @NotBlank(message = "开票产品名称不能为空")
        @Size(max = 32,message = "开票产品名称最大长度不能超过32位")
        private String invoiceProductName;

        /**
        * 类型，（invoiceType字典）
        */
        @NotBlank(message = "类型，（invoiceType字典）不能为空")
        @Size(max = 32,message = "类型，（invoiceType字典）最大长度不能超过32位")
        private String type;


    }

    @Data
    @NoArgsConstructor
    public static class IdsDTO {
        /**
         * 表 ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        /**
         * 是否跳过已生成发票税务信息
         * true 跳过  用于b2c销售订单
         * false 不跳过 用于发票清单列表接口
         */
        private Boolean isCheckInvoiceTax;
    }
}