package com.erp.sdk.oms.amz.spapi.enums.csv;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.erp.sdk.oms.amz.spapi.csv.listing.JPReportListingCsvEntity;
import com.erp.sdk.oms.amz.spapi.csv.listing.ReportListingCsvEntity;
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
public enum ReportListingCsvEnum {

    // 北美
    CA(AmazonMarketplaceEnum.CA, ReportListingCsvEntity.class),
    US(AmazonMarketplaceEnum.US, ReportListingCsvEntity.class),
    MX(AmazonMarketplaceEnum.MX, ReportListingCsvEntity.class),
    BR(AmazonMarketplaceEnum.BR, ReportListingCsvEntity.class),
    // 欧洲
    ES(AmazonMarketplaceEnum.ES, ReportListingCsvEntity.class),
    GB(AmazonMarketplaceEnum.GB, ReportListingCsvEntity.class),
    FR(AmazonMarketplaceEnum.FR, ReportListingCsvEntity.class),
    BE(AmazonMarketplaceEnum.BE, ReportListingCsvEntity.class),
    NL(AmazonMarketplaceEnum.NL, ReportListingCsvEntity.class),
    DE(AmazonMarketplaceEnum.DE, ReportListingCsvEntity.class),
    IT(AmazonMarketplaceEnum.IT, ReportListingCsvEntity.class),
    SE(AmazonMarketplaceEnum.SE, ReportListingCsvEntity.class),
    ZA(AmazonMarketplaceEnum.ZA, ReportListingCsvEntity.class),
    PL(AmazonMarketplaceEnum.PL, ReportListingCsvEntity.class),
    EG(AmazonMarketplaceEnum.EG, ReportListingCsvEntity.class),
    TR(AmazonMarketplaceEnum.TR, ReportListingCsvEntity.class),
    AE(AmazonMarketplaceEnum.AE, ReportListingCsvEntity.class),
    IN(AmazonMarketplaceEnum.IN, ReportListingCsvEntity.class),
    // 分开
    SA(AmazonMarketplaceEnum.SA, ReportListingCsvEntity.class),
    // 远东
    SG(AmazonMarketplaceEnum.SG, ReportListingCsvEntity.class),
    AU(AmazonMarketplaceEnum.AU, ReportListingCsvEntity.class),
    JP(AmazonMarketplaceEnum.JP, JPReportListingCsvEntity.class),

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
        ReportListingCsvEnum listingCsvEnum = Arrays.stream(ReportListingCsvEnum.values())
                .filter(e -> e.getMarketplaceEnum().equals(marketplaceEnum))
                .findFirst().orElse(null);
        return null == listingCsvEnum ? null : listingCsvEnum.getCvsClass();
    }
}
