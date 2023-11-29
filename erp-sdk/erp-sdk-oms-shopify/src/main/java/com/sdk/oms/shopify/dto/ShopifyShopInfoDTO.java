package com.sdk.oms.shopify.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 平台Shopify 店铺 DTO
 *
 * @Author Jim
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ShopifyShopInfoDTO {

    /**
     * 店铺ID
     */
    @Panno(findType = PannoEnum.EQ,field = "id")
    private String id;

    /**
     * 访问token
     */
    @Panno(findType = PannoEnum.EQ,field = "accessToken")
    private String accessToken;

    /**
     * 店铺名称
     */
    @Panno(findType = PannoEnum.EQ,field = "name")
    private String name;

    /**
     * 区域id
     */
    @Panno(findType = PannoEnum.EQ,field = "dictAreaCode")
    private String dictAreaCode;

    /**
     * 国家id
     */
    @Panno(findType = PannoEnum.EQ,field = "dictCountryCode")
    private String dictCountryCode;

    /**
     * 负责人id
     */
    @Panno(findType = PannoEnum.EQ,field = "chargeId")
    private String chargeId;

    /**
     * 店铺全域名
     * SHOP_NAME.myshopify.com
     */
    @Panno(findType = PannoEnum.EQ,field = "shopDomain")
    private String shopDomain;


}
