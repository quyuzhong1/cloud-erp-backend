package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 区域销售分析
 *
 * @Author Jim
 * @Date 2023/09/18
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BiRegionAnalyzeDTO {

    /**
     * 子区域名称
     */
    private String subregionName;

    /**
     * 子区域代号
     */
    private String subregionCode;


    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 区域代号
     */
    private String regionCode;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 全球销售占比
     */
    private BigDecimal globalSalesRatio;


    public static BiRegionAnalyzeDTO init(String regionName,
                                          String regionCode,
                                          String subregionName,
                                          String subregionCode
    ) {
        return new BiRegionAnalyzeDTO()
                .setRegionName(regionName)
                .setRegionCode(regionCode)
                .setSubregionName(subregionName)
                .setSubregionCode(subregionCode)
                .setSalesAmount(BigDecimal.ZERO)
                .setGlobalSalesRatio(BigDecimal.ZERO)
                ;
    }

    public void calculateSalesRatio(BigDecimal total) {
        if ( 0 == BigDecimal.ZERO.compareTo(total)){
            return;
        }
        if ( 0 == BigDecimal.ZERO.compareTo(this.salesAmount)){
            return;
        }
        BigDecimal salesRadio = this.salesAmount.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
        this.setGlobalSalesRatio(salesRadio);
    }
}
