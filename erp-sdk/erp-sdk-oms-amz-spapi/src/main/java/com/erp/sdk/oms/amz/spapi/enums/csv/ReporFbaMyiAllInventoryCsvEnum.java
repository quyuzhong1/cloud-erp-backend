package com.erp.sdk.oms.amz.spapi.enums.csv;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.erp.sdk.oms.amz.spapi.csv.myiall.ReportFbaMyiAllInventoryCsvEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportSuperMongoDTO;
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
public enum ReporFbaMyiAllInventoryCsvEnum {

    // 北美
    CA(AmazonMarketplaceEnum.CA, ReportFbaMyiAllInventoryCsvEntity.class),
    US(AmazonMarketplaceEnum.US, ReportFbaMyiAllInventoryCsvEntity.class),
    MX(AmazonMarketplaceEnum.MX, ReportFbaMyiAllInventoryCsvEntity.class),
    BR(AmazonMarketplaceEnum.BR, ReportFbaMyiAllInventoryCsvEntity.class),
    // 欧洲
    ES(AmazonMarketplaceEnum.ES, ReportFbaMyiAllInventoryCsvEntity.class),
    GB(AmazonMarketplaceEnum.GB, ReportFbaMyiAllInventoryCsvEntity.class),
    FR(AmazonMarketplaceEnum.FR, ReportFbaMyiAllInventoryCsvEntity.class),
    BE(AmazonMarketplaceEnum.BE, ReportFbaMyiAllInventoryCsvEntity.class),
    NL(AmazonMarketplaceEnum.NL, ReportFbaMyiAllInventoryCsvEntity.class),
    DE(AmazonMarketplaceEnum.DE, ReportFbaMyiAllInventoryCsvEntity.class),
    IT(AmazonMarketplaceEnum.IT, ReportFbaMyiAllInventoryCsvEntity.class),
    SE(AmazonMarketplaceEnum.SE, ReportFbaMyiAllInventoryCsvEntity.class),
    ZA(AmazonMarketplaceEnum.ZA, ReportFbaMyiAllInventoryCsvEntity.class),
    PL(AmazonMarketplaceEnum.PL, ReportFbaMyiAllInventoryCsvEntity.class),
    EG(AmazonMarketplaceEnum.EG, ReportFbaMyiAllInventoryCsvEntity.class),
    TR(AmazonMarketplaceEnum.TR, ReportFbaMyiAllInventoryCsvEntity.class),
    AE(AmazonMarketplaceEnum.AE, ReportFbaMyiAllInventoryCsvEntity.class),
    IN(AmazonMarketplaceEnum.IN, ReportFbaMyiAllInventoryCsvEntity.class),
    // 分开
    SA(AmazonMarketplaceEnum.SA, ReportFbaMyiAllInventoryCsvEntity.class),
    // 远东
    SG(AmazonMarketplaceEnum.SG, ReportFbaMyiAllInventoryCsvEntity.class),
    AU(AmazonMarketplaceEnum.AU, ReportFbaMyiAllInventoryCsvEntity.class),
    JP(AmazonMarketplaceEnum.JP, ReportFbaMyiAllInventoryCsvEntity.class),

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
        ReporFbaMyiAllInventoryCsvEnum listingCsvEnum = Arrays.stream(ReporFbaMyiAllInventoryCsvEnum.values())
                .filter(e -> e.getMarketplaceEnum().equals(marketplaceEnum))
                .findFirst().orElse(null);
        return null == listingCsvEnum ? null : listingCsvEnum.getCvsClass();
    }
}
