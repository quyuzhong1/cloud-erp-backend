package com.erp.model.oms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * KOL回片费用表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class KolFeedbackCostDTO implements Serializable {




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
        * 回片链接（完整链接）
        */
        private String url;

        /**
        * 回片链接哈希值（MD5或SHA256，用于唯一键）
        */
        private String urlHash;

        /**
        * 费用名称
        */
        private String costType;

        /**
        * 费用名称ID
        */
        private String costTypeId;

        /**
        * 付费币别
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 金额（原币）
        */
        private BigDecimal originalAmount;

        /**
        * 金额（本位币）
        */
        private BigDecimal baseAmount;

        /**
        * 备注
        */
        private String remark;


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
        * 回片链接（完整链接）
        */
        @NotBlank(message = "回片链接不能为空")
        private String url;

        /**
        * 回片链接哈希值（MD5或SHA256，用于唯一键）
        */
        @Size(max = 64,message = "回片链接哈希值（MD5或SHA256，用于唯一键）最大长度不能超过64位")
        private String urlHash;

        /**
        * 费用名称
        */
        @Size(max = 100,message = "费用名称最大长度不能超过100位")
        private String costType;

        /**
        * 费用名称ID
        */
        @NotBlank(message = "费用名称ID不能为空")
        @Size(max = 19,message = "费用名称ID最大长度不能超过19位")
        private String costTypeId;

        /**
        * 付费币别
        */
        @NotBlank(message = "付费币别不能为空")
        @Size(max = 30,message = "付费币别最大长度不能超过30位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 10, fraction = 6, message = "汇率整数位不能超过10位，小数位不能超过6位")
        private BigDecimal exchangeRate;

        /**
        * 金额（原币）
        */
        @NotNull(message = "金额（原币）不能为空")
        @Digits(integer = 12, fraction = 6, message = "金额（原币）整数位不能超过12位，小数位不能超过6位")
        private BigDecimal originalAmount;

        /**
        * 金额（本位币）
        */
        @Digits(integer = 12, fraction = 6, message = "金额（本位币）整数位不能超过12位，小数位不能超过6位")
        private BigDecimal baseAmount;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}