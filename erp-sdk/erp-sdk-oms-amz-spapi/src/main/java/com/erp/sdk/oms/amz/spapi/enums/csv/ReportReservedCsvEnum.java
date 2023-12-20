package com.erp.sdk.oms.amz.spapi.enums.csv;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.erp.sdk.oms.amz.spapi.csv.reserved.ReportReservedCsvEntity;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 市场对应CSV实体类型
 *
 * @author Jim
 * @date 2023/11/1
 */
@Getter
@AllArgsConstructor
public enum ReportReservedCsvEnum {

    // 北美
    CA(AmazonMarketplaceEnum.CA, ReportReservedCsvEntity.class),
    US(AmazonMarketplaceEnum.US, ReportReservedCsvEntity.class),
    MX(AmazonMarketplaceEnum.MX, ReportReservedCsvEntity.class),
    BR(AmazonMarketplaceEnum.BR, ReportReservedCsvEntity.class),
    // 欧洲
    ES(AmazonMarketplaceEnum.ES, ReportReservedCsvEntity.class),
    GB(AmazonMarketplaceEnum.GB, ReportReservedCsvEntity.class),
    FR(AmazonMarketplaceEnum.FR, ReportReservedCsvEntity.class),
    BE(AmazonMarketplaceEnum.BE, ReportReservedCsvEntity.class),
    NL(AmazonMarketplaceEnum.NL, ReportReservedCsvEntity.class),
    DE(AmazonMarketplaceEnum.DE, ReportReservedCsvEntity.class),
    IT(AmazonMarketplaceEnum.IT, ReportReservedCsvEntity.class),
    SE(AmazonMarketplaceEnum.SE, ReportReservedCsvEntity.class),
    ZA(AmazonMarketplaceEnum.ZA, ReportReservedCsvEntity.class),
    PL(AmazonMarketplaceEnum.PL, ReportReservedCsvEntity.class),
    EG(AmazonMarketplaceEnum.EG, ReportReservedCsvEntity.class),
    TR(AmazonMarketplaceEnum.TR, ReportReservedCsvEntity.class),
    AE(AmazonMarketplaceEnum.AE, ReportReservedCsvEntity.class),
    IN(AmazonMarketplaceEnum.IN, ReportReservedCsvEntity.class),
    // 分开
    SA(AmazonMarketplaceEnum.SA, ReportReservedCsvEntity.class),
    // 远东
    SG(AmazonMarketplaceEnum.SG, ReportReservedCsvEntity.class),
    AU(AmazonMarketplaceEnum.AU, ReportReservedCsvEntity.class),
    JP(AmazonMarketplaceEnum.JP, ReportReservedCsvEntity.class),

    ;


    /**
     * 国家代号
     */
    @EnumValue
    @JsonValue
    private final AmazonMarketplaceEnum marketplaceEnum;

    /**
     * CSV实体
     */
    private final Class<?> cvsClass;


    public static Class<?> getCvsClassByMarketplace(AmazonMarketplaceEnum marketplaceEnum) {
        ReportReservedCsvEnum listingCsvEnum = Arrays.stream(ReportReservedCsvEnum.values())
                .filter(e -> e.getMarketplaceEnum().equals(marketplaceEnum))
                .findFirst().orElse(null);
        return null == listingCsvEnum ? null : listingCsvEnum.getCvsClass();
    }
}
