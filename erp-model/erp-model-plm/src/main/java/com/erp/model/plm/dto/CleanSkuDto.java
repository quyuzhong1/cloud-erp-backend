package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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


}
