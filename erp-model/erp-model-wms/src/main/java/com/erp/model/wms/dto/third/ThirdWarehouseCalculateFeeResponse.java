package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThirdWarehouseCalculateFeeResponse {

    //第三方仓的入库单号
    private String receivingCode;

    /**
     * 物流商名称
     */
    private String logisticsName;

    /**
     * 物流渠道名称名称
     */
    private String channelName;
    /**
     * 物流渠道名称名称英文
     */
    private String channelNameEn;

    /**
     * 物流渠道id
     */
    private String channelId;
    /**
     * 物流渠道编码
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

}
