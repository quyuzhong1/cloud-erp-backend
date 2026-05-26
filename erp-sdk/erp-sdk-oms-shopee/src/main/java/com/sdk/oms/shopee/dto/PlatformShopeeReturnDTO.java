package com.sdk.oms.shopee.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;

/**
 * Shopee 售后退货 BI 清洗 DTO
 */
@Data
public class PlatformShopeeReturnDTO {

    @Panno(value = PannoEnum.EQ)
    private String returnSn;

    @Panno(value = PannoEnum.EQ)
    private String orderSn;

    @Panno(value = PannoEnum.EQ)
    private String shopId;
}
