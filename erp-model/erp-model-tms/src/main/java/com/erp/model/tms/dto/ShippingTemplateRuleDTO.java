package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 运费模板渠道关联表请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@NoArgsConstructor
public class ShippingTemplateRuleDTO implements Serializable {




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
        * 起始地
        */
        private String fromCountry;

        /**
         * 起始地名称
         */
        private String fromCountryName;

        /**
        * 目的地
        */
        private String toCountry;

        /**
         * 目的地名称
         */
        private String toCountryName;

        /**
        * 分区
        */
        private String region;

        /**
         * 城市
         */
        private List<String> cityList;

        /**
        * 目的仓库
        */
        private String toWarehouseName;

        /**
        * 开始重量
        */
        private BigDecimal startWeight;

        /**
        * 结束重量
        */
        private BigDecimal endWeight;

        /**
        * 首重
        */
        private BigDecimal firstWeight;

        /**
        * 首重运费
        */
        private BigDecimal firstWeightShippingCost;

        /**
        * 续重单位重量
        */
        private BigDecimal additionalUnitWeight;

        /**
        * 续重单价
        */
        private BigDecimal additionalPrice;

        /**
        * 挂号费
        */
        private BigDecimal registrationCost;

        /**
        * 操作费
        */
        private BigDecimal operatingCost;

        /**
        * 最低收费
        */
        private BigDecimal minCost;


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
        * 起始地
        */
        @NotBlank(message = "起始地不能为空")
        @Size(max = 32,message = "起始地最大长度不能超过32位")
        private String fromCountry;

        /**
        * 目的地
        */
        @Size(max = 32,message = "目的地最大长度不能超过32位")
        private String toCountry;

        /**
        * 分区
        */
        @Size(max = 64,message = "分区最大长度不能超过64位")
        private String region;

        /**
         * 城市
         */
        private List<String> cityList;

        /**
        * 目的仓库
        */
        @Size(max = 64,message = "目的仓库最大长度不能超过64位")
        private String toWarehouseName;

        /**
        * 开始重量
        */
        @NotNull(message = "开始重量不能为空")
        private BigDecimal startWeight;

        /**
        * 结束重量
        */
        @NotNull(message = "结束重量不能为空")
        private BigDecimal endWeight;

        /**
        * 首重
        */
        private BigDecimal firstWeight;

        /**
        * 首重运费
        */
        @Digits(integer = 12, fraction = 4, message = "首重运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal firstWeightShippingCost;

        /**
        * 续重单位重量
        */
        @Digits(integer = 12, fraction = 4, message = "续重单位重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal additionalUnitWeight;

        /**
        * 续重单价
        */
        @Digits(integer = 12, fraction = 4, message = "续重单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal additionalPrice;

        /**
         * 运费单价
         */
        @Digits(integer = 12, fraction = 4, message = "运费单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingPrice;

        /**
        * 挂号费
        */
        @NotNull(message = "挂号费不能为空")
        @Digits(integer = 12, fraction = 4, message = "挂号费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal registrationCost;

        /**
        * 操作费
        */
        @NotNull(message = "操作费不能为空")
        @Digits(integer = 12, fraction = 4, message = "操作费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal operatingCost;

        /**
        * 最低收费
        */
        @NotNull(message = "最低收费不能为空")
        @Digits(integer = 12, fraction = 4, message = "最低收费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal minCost;


    }

    @Data
    @NoArgsConstructor
    public static class ViewParamDTO {
        /**
         * 主表id
         */
        private String mainId;

        /**
         * 起始地
         */
        private String fromCountry;

        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 分区
         */
        private String region;

        /**
         * 目的仓库
         */
        private String toWarehouseName;
    }

}