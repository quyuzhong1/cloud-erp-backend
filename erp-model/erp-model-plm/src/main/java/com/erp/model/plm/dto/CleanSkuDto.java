package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CleanSkuDto {
    /**
     * 品牌id
     */
    private String brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 品类id
     */
    private String categoryId;

    /**
     * 品类名称
     */
    private String categoryName;

    /**
     * 产品上市时间
     */
    private LocalDate listingTime;


}
