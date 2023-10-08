package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 国家销售分析
 *
 * @Author Jim
 * @Date 2023/09/18
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BiCountryAnalyzeDTO {

    /**
     * 国家中文名称
     */
    private String countryNameCn;

    /**
     * 国家英文名称
     */
    private String countryNameEn;

    /**
     * 国家代号
     */
    private String countryCode;

    /**
     * 国家所属区域名称
     */
    private String regionName;

    /**
     * 国家所属区域代号
     */
    private String regionCode;

    /**
     * 国家所属子区域名称
     */
    private String subregionName;

    /**
     * 国家所属子区域代号
     */
    private String subregionCode;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 全球销售占比
     */
    private BigDecimal globalSalesRatio;

    /**
     * 国家所属区域销售占比
     */
    private BigDecimal regionSalesRatio;

    /**
     * 国家所属子区域销售占比
     */
    private BigDecimal subregionSalesRatio;


    /**
     * 初始化
     */
    public static BiCountryAnalyzeDTO init(String countryNameCn,
                                           String countryNameEn,
                                           String countryCode,
                                           String regionName,
                                           String regionCode,
                                           String subregionName,
                                           String subregionCode,
                                           BigDecimal salesAmount
    ) {
        return new BiCountryAnalyzeDTO()
                .setCountryNameCn(countryNameCn)
                .setCountryNameEn(countryNameEn)
                .setCountryCode(countryCode)
                .setRegionName(regionName)
                .setRegionCode(regionCode)
                .setSubregionName(subregionName)
                .setSubregionCode(subregionCode)
                .setSalesAmount(salesAmount)
                .setGlobalSalesRatio(BigDecimal.ZERO)
                .setRegionSalesRatio(BigDecimal.ZERO)
                .setSubregionSalesRatio(BigDecimal.ZERO)
                ;
    }

    /**
     * 设置所有占比
     */
    public void setAllRadio(BigDecimal globalTotal, BigDecimal regionTotal, BigDecimal subregionTotal) {
        if (0 == this.salesAmount.compareTo(BigDecimal.ZERO)){
            return;
        }
        // 设置全球占比
        if (0 != globalTotal.compareTo(BigDecimal.ZERO)){
            BigDecimal globalSalesRadio = this.salesAmount.divide(globalTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            this.setGlobalSalesRatio(globalSalesRadio);
        }
        // 设置区域占比
        if (0 != regionTotal.compareTo(BigDecimal.ZERO)){
            BigDecimal regionSalesRadio = this.salesAmount.divide(regionTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            this.setRegionSalesRatio(regionSalesRadio);
        }
        // 设置区域占比
        if (0 != subregionTotal.compareTo(BigDecimal.ZERO)){
            BigDecimal subregionSalesRadio = this.salesAmount.divide(subregionTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            this.setSubregionSalesRatio(subregionSalesRadio);
        }
    }
}
