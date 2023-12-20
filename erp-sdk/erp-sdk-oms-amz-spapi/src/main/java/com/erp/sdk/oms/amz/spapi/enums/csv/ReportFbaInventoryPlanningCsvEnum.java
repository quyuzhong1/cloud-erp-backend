package com.erp.sdk.oms.amz.spapi.enums.csv;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.erp.sdk.oms.amz.spapi.csv.planning.ReportFbaInventoryPlanningCsvEntity;
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
public enum ReportFbaInventoryPlanningCsvEnum {

    // 北美
    CA(AmazonMarketplaceEnum.CA, ReportFbaInventoryPlanningCsvEntity.class),
    US(AmazonMarketplaceEnum.US, ReportFbaInventoryPlanningCsvEntity.class),
    MX(AmazonMarketplaceEnum.MX, ReportFbaInventoryPlanningCsvEntity.class),
    BR(AmazonMarketplaceEnum.BR, ReportFbaInventoryPlanningCsvEntity.class),
    // 欧洲
    ES(AmazonMarketplaceEnum.ES, ReportFbaInventoryPlanningCsvEntity.class),
    GB(AmazonMarketplaceEnum.GB, ReportFbaInventoryPlanningCsvEntity.class),
    FR(AmazonMarketplaceEnum.FR, ReportFbaInventoryPlanningCsvEntity.class),
    BE(AmazonMarketplaceEnum.BE, ReportFbaInventoryPlanningCsvEntity.class),
    NL(AmazonMarketplaceEnum.NL, ReportFbaInventoryPlanningCsvEntity.class),
    DE(AmazonMarketplaceEnum.DE, ReportFbaInventoryPlanningCsvEntity.class),
    IT(AmazonMarketplaceEnum.IT, ReportFbaInventoryPlanningCsvEntity.class),
    SE(AmazonMarketplaceEnum.SE, ReportFbaInventoryPlanningCsvEntity.class),
    ZA(AmazonMarketplaceEnum.ZA, ReportFbaInventoryPlanningCsvEntity.class),
    PL(AmazonMarketplaceEnum.PL, ReportFbaInventoryPlanningCsvEntity.class),
    EG(AmazonMarketplaceEnum.EG, ReportFbaInventoryPlanningCsvEntity.class),
    TR(AmazonMarketplaceEnum.TR, ReportFbaInventoryPlanningCsvEntity.class),
    AE(AmazonMarketplaceEnum.AE, ReportFbaInventoryPlanningCsvEntity.class),
    IN(AmazonMarketplaceEnum.IN, ReportFbaInventoryPlanningCsvEntity.class),
    // 分开
    SA(AmazonMarketplaceEnum.SA, ReportFbaInventoryPlanningCsvEntity.class),
    // 远东
    SG(AmazonMarketplaceEnum.SG, ReportFbaInventoryPlanningCsvEntity.class),
    AU(AmazonMarketplaceEnum.AU, ReportFbaInventoryPlanningCsvEntity.class),
    JP(AmazonMarketplaceEnum.JP, ReportFbaInventoryPlanningCsvEntity.class),

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
        ReportFbaInventoryPlanningCsvEnum listingCsvEnum = Arrays.stream(ReportFbaInventoryPlanningCsvEnum.values())
                .filter(e -> e.getMarketplaceEnum().equals(marketplaceEnum))
                .findFirst().orElse(null);
        return null == listingCsvEnum ? null : listingCsvEnum.getCvsClass();
    }
}
