package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ProductBaseInfoDTO {
    @ApiModelProperty(value = "产品spu基础信息",required = true)
    private ProductInfoDTO productSpuBaseInfoDTO;

    @ApiModelProperty(value = "产品sku基础信息",required = true)
    private ProductSkuBaseInfoDTO productSkuBaseInfoDTO;
}
