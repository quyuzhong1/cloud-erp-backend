package com.sdk.oms.shopee.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;

/**
 * Shopee 售后退货 BI 清洗 DTO
 */
@Data
public class PlatformShopeeReturnDTO {

    @Panno(findType = PannoEnum.EQ, field = "returnSn")
    private String returnSn;

    @Panno(findType = PannoEnum.EQ, field = "orderSn")
    private String orderSn;

    @Panno(findType = PannoEnum.EQ, field = "shopId")
    private String shopId;
}
