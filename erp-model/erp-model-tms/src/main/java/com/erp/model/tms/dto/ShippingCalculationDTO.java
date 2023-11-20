package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算DTO
 * @date 2023/11/9 17:47
 */
@Data
public class ShippingCalculationDTO {


    /**
     * 查询条件
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 发货方式
         */
        private String shipmentMethod;

        /**
         * 起始地
         */
        private List<String> fromCountryList;

        /**
         * 目的地
         */
        private List<String> toCountryList;

        /**
         * 目的仓库 http://172.16.100.11:3002/project/128/interface/api/25567
         */
        private List<String> toWarehouseList;

        /**
         * 城市
         */
        private String city;

        /**
         * 物流渠道id集合
         */
        private List<String> channelIdList;

        /**
         * 重量
         */
        @NotNull(message = "重量不能为空")
        private BigDecimal weight;

        /**
         * 重量单位
         */
        @NotBlank(message = "重量单位不能为空")
        private String weightUnit;

        /**
         * 长
         */
        private BigDecimal length;

        /**
         * 宽
         */
        private BigDecimal width;

        /**
         * 高
         */
        private BigDecimal height;
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 物流商名称
         */
        private String logisticsName;

        /**
         * 物流渠道名称名称
         */
        private String channelName;

        /**
         * 计费规则
         */
        private String feeRule;

        /**
         * 目的仓库
         */
        private String toWarehouseName;

        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 分区
         */
        private String region;

        /**
         * 有效期
         */
        private String effectivePeriod ;

        /**
         * 生效日期
         */
        private LocalDate effectiveDate ;

        /**
         * 失效日期
         */
        private LocalDate expireDate ;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 材积设置
         */
        private Integer volumeSetting;

        /**
         * 运费
         */
        private BigDecimal shippingCost;

        /**
         * 挂号费
         */
        private BigDecimal registrationCost;

        /**
         * 操作费
         */
        private BigDecimal operatingCost;

        /**
         * 其他费用
         */
        private OtherCostDTO otherCostDTO;

        /**
         * 其他费用
         */
        private String otherCostStr;

        /**
         * 总金额
         */
        private BigDecimal totalShippingCost;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 模板对象
         */
        private ShippingTemplateEntity templateEntity;

        /**
         * 模板规则
         */
        private ShippingTemplateRuleEntity templateRuleEntity;
    }

    /**
     * 其他费用
     */
    @Data
    @NoArgsConstructor
    public static class OtherCostDTO {

        /**
         * 折扣费
         */
        private BigDecimal discountCost;

        /**
         * 超尺寸附加费
         */
        private BigDecimal oversizeSurchargeCost;

        /**
         * 签名费
         */
        private BigDecimal signatureCost;

        /**
         * 燃油附加费
         */
        private BigDecimal fuelSurchargeCost;

        /**
         * 保险费
         */
        private BigDecimal premiumCost;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 运费
         */
        private BigDecimal shippingCost;

        /**
         * 操作费
         */
        private BigDecimal operatingCost;

        /**
         * 挂号费
         */
        private BigDecimal registrationCost;

        /**
         * 折扣费
         */
        private BigDecimal discountCost;

        /**
         * 超尺寸附加费
         */
        private BigDecimal oversizeSurchargeCost;

        /**
         * 签名费
         */
        private BigDecimal signatureCost;

        /**
         * 燃油附加费
         */
        private BigDecimal fuelSurchargeCost;

        /**
         * 保险费
         */
        private BigDecimal premiumCost;

        /**
         * 最终运费(运费计算)
         */
        private BigDecimal totalShippingCost;

        /**
         * 最终运费（运费试算）
         */
        private BigDecimal totalTrialShippingCost;
    }

    /**
     * 查询城市参数
     */
    @Data
    @NoArgsConstructor
    public static class ListRegionCityParamDTO {

        /**
         * 模板规则 id
         */
        private String templateRuleId;

        /**
         * 分区
         */
        private String region;
    }

}
