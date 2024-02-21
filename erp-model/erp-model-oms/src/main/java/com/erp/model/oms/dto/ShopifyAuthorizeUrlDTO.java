package com.erp.model.oms.dto;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.oms.entity.ShopInfoEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 店铺授权DTO
 */
@Data
@NoArgsConstructor
public class ShopifyAuthorizeUrlDTO implements Serializable {

    /**
     * hmac
     */
    @NotNull(message = "hmac不能为空")
    private String hmac;
    /**
     * 店铺地址
     */
    @NotNull(message = "host不能为空")
    private String host;
    /**
     * 店铺域名
     */
    @NotNull(message = "shop不能为空")
    private String shop;
    /**
     * 时间戳
     */
    @NotNull(message = "timestamp不能为空")
    private String timestamp;

}
