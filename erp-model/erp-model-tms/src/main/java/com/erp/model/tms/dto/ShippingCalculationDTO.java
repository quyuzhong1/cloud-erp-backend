package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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

    private ShippingCalculationDTO() {
    }

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
         * 省/州
         */
        private String province;

        /**
         * 物流渠道id集合
         */
        private List<String> channelIdList;
        /**
         * 渠道编码
         */
        private List<String> channelCodeList;

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

        /**
         * 体积
         */
        private BigDecimal volume;

        /**
         * 发货仓库id(自发货类型下必填)
         * http://172.16.100.11:3002/project/128/interface/api/25567
         */
        private String fromWarehouseId;
        /**
         * 销售订单id
         */
        private String b2cSoId;
        /**
         * 邮编
         */
        private String postCode;
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
         * 物流渠道id
         */
        private String channelId;
        /**
         * 渠道编码
         */
        private String channelCode;

        /**
         * 计费规则
         */
        private String feeRule;

        /**
         * 是否签名
         */
        private Boolean isApiSign;

        /**
         * 是否保险
         */
        private Boolean isApiInsurance;

        /**
         * 目的仓库
         */
        private String toWarehouseName;

        /**
         * 目的地
         */
        private String toCountry;
        /**
         * 目的国家名称
         */
        private String toCountryName;

        /**
         * 分区
         */
        private String region;

        /**
         * 有效期
         */
        private String effectivePeriod;
        /**
         * 时效
         */
        private String effectiveTime;
        /**
         * 时效单位
         */
        private String effectiveTimeUnit;
        /**
         * 时效  时效取值为“物流渠道管理”对应渠道的时效 effective_time_unit
         */
        private String effectiveTimeStr;

        /**
         * 生效日期
         */
        private LocalDate effectiveDate;

        /**
         * 失效日期
         */
        private LocalDate expireDate;

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
         * 报关费用  新增字段，目前无计算规则取值显示为0
         */
        private BigDecimal declareCost;

        /**
         * 其他费用
         * 新增字段，按照计算模板计算类型【超尺寸附加费+签名费+燃油附加费+保险费】【若有折扣则按照折扣计算】
         */
        private BigDecimal otherCost;

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
         * 模板id
         */
        private String templateId;
        /**
         * 重量段类型
         */
        private String billingMethod;
        /**
         * 进制
         */
        private String priceBinary;
        /**
         * 进制
         */
        private String templateRuleId;
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
         * 运费单价
         */
        private BigDecimal shippingPrice;
        /**
         * 最低收费
         */
        private BigDecimal minCost;

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

    /**
     * 渠道费用
     */
    @Data
    @NoArgsConstructor
    public static class ChannelCostDTO {

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名
         */
        private String logisticsChannelName;

        /**
         * 物流费
         */
        private BigDecimal shippingCost;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

    }


    /**
     * 运费测算结果
     */
    @Data
    @NoArgsConstructor
    public static class CostCalculationResultDTO {


        /**
         * 国家
         */
        private String country;


        /**
         * 国家名
         */
        private String countryName;

        /**
         * 包装重量
         */
        private BigDecimal weight;

        /**
         * 单位重量
         */
        private String weightUnit;

        /**
         * 费用列表
         */
        List<ChannelCostDTO> costList;

    }

}
