package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CfgInvoiceSettingDetailDTO implements Serializable {
    @Data
    @NoArgsConstructor
    public static class CommonDTO {

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
        @Size(max = 19, message = "平台value最大长度不能超过19位")
        private String platformValue;

        /**
         * 店铺id
         */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19, message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
         * 发票设置id
         */
        @NotBlank(message = "发票设置id不能为空")
        @Size(max = 19, message = "发票设置id最大长度不能超过19位")
        private String mainId;

        /**
         * 开票规则
         */
        @NotBlank(message = "开票规则不能为空")
        @Size(max = 50, message = "开票规则最大长度不能超过50位")
        private String dictInvoiceRule;

        /**
         * 比例
         */
        @NotNull(message = "比例不能为空")
        private BigDecimal ratio;

        /**
         * 是否包含运费
         */
        @NotNull(message = "是否包含运费不能为空")
        private Boolean isContainShipFee;

        /**
         * 税费类型
         */
        @NotBlank(message = "税费类型不能为空")
        @Size(max = 50, message = "税费类型最大长度不能超过50位")
        private String taxType;

        /**
         * 开票节点（自动开票）
         */
        @NotBlank(message = "开票节点不能为空")
        @Size(max = 50, message = "开票节点最大长度不能超过50位")
        private String invoiceNode;

        /**
         * 自动上传
         */
        @NotNull(message = "自动上传不能为空")
        private Boolean isAutoUpload;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CfgInvoiceSettingDetailDTO.CommonDTO {

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CfgInvoiceSettingDetailDTO.CommonDTO {

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetailShop{
        /**
         * 主键id
         */
        private String  mainId;
        /**
         * 开票节点（自动开票）
         */
        private String dictPlatform;

        /**
         * 自动上传
         */
        private String name;
    }
}
