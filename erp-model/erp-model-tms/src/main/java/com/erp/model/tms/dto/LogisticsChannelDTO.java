package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 物流渠道表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class LogisticsChannelDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 渠道名称
        */
        private String name;

        /**
        * 渠道代码
        */
        private String code;

        /**
        * 时效
        */
        private Integer effectiveTime;

        /**
        * 时效单位
        */
        private String effectiveTimeUnit;

        /**
        * 物流轨迹查询方式
        */
        private String trackQueryMode;

        /**
        * 纸张长
        */
        private Integer paperLength;

        /**
        * 纸张宽
        */
        private Integer paperWidth;

        /**
        * 分拣码
        */
        private String sortingCode;

        /**
        * 运费模板id
        */
        private String shippingTemplateId;

        /**
        * 费用规则
        */
        private String feeRule;

        /**
        * 最高报关金额
        */
        private BigDecimal maxCustomsAmount;

        /**
        * 最高报关币别
        */
        private String maxCustomsCurrency;

        /**
        * 最低报关金额
        */
        private BigDecimal minCustomsAmount;

        /**
        * 最低报关币种
        */
        private String minCustomsCurrency;

        /**
        * 重量上限
        */
        private BigDecimal maxWeight;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 税费模式
        */
        private String taxModel;

        /**
        * 是否ioss 预交
        */
        private Boolean isIossPrepay;

        /**
        * 是否签名服务
        */
        private Boolean isApiSign;

        /**
        * 是否保险
        */
        private Boolean isApiInsurance;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 渠道名称
        */
        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 100,message = "渠道名称最大长度不能超过100位")
        private String name;

        /**
        * 时效
        */
        @NotNull(message = "时效不能为空")
        private Integer effectiveTime;

        /**
        * 时效单位
        */
        @NotBlank(message = "时效单位不能为空")
        @Size(max = 30,message = "时效单位最大长度不能超过30位")
        private String effectiveTimeUnit;

        /**
        * 物流轨迹查询方式
        */
        @NotBlank(message = "物流轨迹查询方式不能为空")
        @Size(max = 30,message = "物流轨迹查询方式最大长度不能超过30位")
        private String trackQueryMode;

        /**
        * 纸张长
        */
        @NotNull(message = "纸张长不能为空")
        private Integer paperLength;

        /**
        * 纸张宽
        */
        @NotNull(message = "纸张宽不能为空")
        private Integer paperWidth;

        /**
        * 分拣码
        */
        @NotBlank(message = "分拣码不能为空")
        @Size(max = 30,message = "分拣码最大长度不能超过30位")
        private String sortingCode;

        /**
        * 运费模板id
        */
        @NotBlank(message = "运费模板id不能为空")
        @Size(max = 19,message = "运费模板id最大长度不能超过19位")
        private String shippingTemplateId;

        /**
        * 费用规则
        */
        @NotBlank(message = "费用规则不能为空")
        @Size(max = 30,message = "费用规则最大长度不能超过30位")
        private String feeRule;

        /**
        * 最高报关金额
        */
        @NotNull(message = "最高报关金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "最高报关金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal maxCustomsAmount;

        /**
        * 最高报关币别
        */
        @NotBlank(message = "最高报关币别不能为空")
        @Size(max = 30,message = "最高报关币别最大长度不能超过30位")
        private String maxCustomsCurrency;

        /**
        * 最低报关金额
        */
        @NotNull(message = "最低报关金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "最低报关金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal minCustomsAmount;

        /**
        * 最低报关币种
        */
        @NotBlank(message = "最低报关币种不能为空")
        @Size(max = 30,message = "最低报关币种最大长度不能超过30位")
        private String minCustomsCurrency;

        /**
        * 重量上限
        */
        @NotNull(message = "重量上限不能为空")
        @Digits(integer = 12, fraction = 4, message = "重量上限整数位不能超过12位，小数位不能超过4位")
        private BigDecimal maxWeight;

        /**
        * 重量单位
        */
        @NotBlank(message = "重量单位不能为空")
        @Size(max = 10,message = "重量单位最大长度不能超过10位")
        private String weightUnit;

        /**
        * 税费模式
        */
        @NotBlank(message = "税费模式不能为空")
        @Size(max = 30,message = "税费模式最大长度不能超过30位")
        private String taxModel;

        /**
        * 是否ioss 预交
        */
        @NotNull(message = "是否ioss 预交不能为空")
        private Boolean isIossPrepay;

        /**
        * 是否签名服务
        */
        @NotNull(message = "是否签名服务不能为空")
        private Boolean isApiSign;

        /**
        * 是否保险
        */
        @NotNull(message = "是否保险不能为空")
        private Boolean isApiInsurance;


    }

    @Data
    @NoArgsConstructor
    public static class ListSelectDTO {

        /**
         * 渠道名称
         */
        private String name;
        /**
         * 渠道编码
         */
        private String code;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

}