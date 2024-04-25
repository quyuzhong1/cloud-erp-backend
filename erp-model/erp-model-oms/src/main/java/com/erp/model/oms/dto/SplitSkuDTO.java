package com.erp.model.oms.dto;

import com.erp.model.oms.enums.CalculateRuleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName SplitSkuDTO
 * @description: TODO
 * @date 2024年01月30日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SplitSkuDTO {

    private String skuId;

    private String skuNo;

    private Integer qty;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;

    private BigDecimal grossWeight;

    public SplitSkuDTO(String skuId, String skuNo) {
        this.skuId = skuId;
        this.skuNo = skuNo;
        this.qty = 0;
        this.length = BigDecimal.ZERO;
        this.width = BigDecimal.ZERO;
        this.height = BigDecimal.ZERO;
        this.grossWeight = BigDecimal.ZERO;
    }

    public static BigDecimal calculateSplitSkuDTOGrossWeight(List<SplitSkuDTO> skuList, String grossWeight) {
        BigDecimal totalGrossWeight;
        //毛重
        if (StringUtils.isNotEmpty(grossWeight) && CalculateRuleEnum.MAX.getCode().equalsIgnoreCase(grossWeight)) {
            totalGrossWeight = skuList.stream().map(SplitSkuDTO::getGrossWeight)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else if (StringUtils.isNotEmpty(grossWeight) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(grossWeight)) {
            totalGrossWeight = skuList.stream().map(SplitSkuDTO::getGrossWeight)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            totalGrossWeight = skuList.stream().map(e -> e.getGrossWeight().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
        return totalGrossWeight;
    }
}
