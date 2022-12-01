package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductManySpecUpdateDTO {
    /**
     * sku表id
     */
    private String id;

    /**
     * 产品信息表id
     */
    private String productId;
}
