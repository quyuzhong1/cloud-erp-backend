package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * bi销售额占比DTO
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BiSalesRadioDTO {
    /**
     * 平台名称
     */
    public String platform;
    /**
     * 销售额
     */
    public String sales;
    /**
     * 销售额占比: 10=10%
     */
    public String salesRadio;



    public static BiSalesRadioDTO init(Map<String, Object> map) {
        return new BiSalesRadioDTO()
                .setPlatform((String) map.get("platform"))
                .setSales(((BigDecimal) map.get("sales")).toPlainString())
                .setSalesRadio("0")
                ;
    }

    /**
     * 计算和设置占比
     */
    public void setRadioBySumSumNumber(BigDecimal finalSumNumber) {
        if (finalSumNumber.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal radio = new BigDecimal(this.sales)
                .divide(finalSumNumber, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                ;
        this.salesRadio = radio.stripTrailingZeros().toPlainString();

    }
}
