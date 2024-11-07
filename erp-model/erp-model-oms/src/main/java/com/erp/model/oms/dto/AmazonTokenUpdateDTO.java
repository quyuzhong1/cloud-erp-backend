package com.erp.model.oms.dto;

import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 亚马逊token更新
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AmazonTokenUpdateDTO implements Serializable {


    /**
     * 当前店铺信息
     */
    private ShopInfoEntity shopInfo;

    /**
     * 当前店铺授权
     */
    private ShopAuthEntity shopAuth;

    /**
     * 短令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

}
