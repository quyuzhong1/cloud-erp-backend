package com.cloud.erp.service.impl;

import com.cloud.erp.constant.ShopifyConstant;
import com.cloud.erp.service.ShopServer;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Lambda
 * @Classname ShopServerImpl
 * @Description TODO
 * @Date 2023-08-28 16:37
 * @Created by yl
 */
@Service
@Slf4j
public class ShopServerImpl implements ShopServer {

    @Override
    public String getShopAuthorizeUrl(CfgAppClientEntity entity,String shopName) {
        if (Objects.isNull(entity)) {
            return "";
        }
        String grantOptions = "per-user";
        String shopAuthorizeUrl = String.format(entity.getUrl(), shopName, entity.getClientId(), grantOptions, entity.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE);
        return null;
    }
}
