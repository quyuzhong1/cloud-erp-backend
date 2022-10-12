package com.erp.model.plm.dto;

import lombok.Data;

@Data
public class ProductBaseInfoDTO {
    /**
     * 产品spu基础信息
     */
    private ProductInfoDTO productSpuBaseInfoDTO;

    /**
     * 产品sku基础信息
     */
    private ProductSkuBaseInfoDTO productSkuBaseInfoDTO;
}
