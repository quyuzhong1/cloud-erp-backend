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

    public static BigDecimal calculateSplitSkuDTOHeight(List<SplitSkuDTO> skuList, String height) {
        BigDecimal totalHeight;
        //高
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(height) && CalculateRuleEnum.MAX.getCode().equalsIgnoreCase(height)) {
            totalHeight = skuList.stream().map(SplitSkuDTO::getHeight)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(height) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(height)) {
            totalHeight = skuList.stream().map(SplitSkuDTO::getHeight)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            totalHeight = skuList.stream().map(e -> e.getHeight().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
        return totalHeight;
    }

    public static BigDecimal calculateSplitSkuDTOWidth(List<SplitSkuDTO> skuList, String width) {
        BigDecimal maxWidth;
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(width) && CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(width)) {
            maxWidth = skuList.stream().map(e -> e.getWidth().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(width) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(width)) {
            maxWidth = skuList.stream().map(SplitSkuDTO::getWidth)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            maxWidth = skuList.stream().map(SplitSkuDTO::getWidth)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        }
        return maxWidth;
    }

    public static BigDecimal calculateSplitSkuDTOLength(List<SplitSkuDTO> skuList, String length) {
        BigDecimal maxLength;
        //长
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(length) && CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(length)) {
            maxLength = skuList.stream()
                    .map(e -> e.getLength().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(length) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(length)) {
            maxLength = skuList.stream().map(SplitSkuDTO::getLength)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            maxLength = skuList.stream().map(SplitSkuDTO::getLength)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        }
        return maxLength;
    }
}
