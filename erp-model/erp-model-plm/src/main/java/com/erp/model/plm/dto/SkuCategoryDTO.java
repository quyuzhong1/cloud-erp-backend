package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SkuCategoryDTO
 * @Description TODO
 * @Date 2023-03-10 14:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuCategoryDTO  implements Serializable {

    private String productId;

    private String skuId;

    private String categoryId;
}
