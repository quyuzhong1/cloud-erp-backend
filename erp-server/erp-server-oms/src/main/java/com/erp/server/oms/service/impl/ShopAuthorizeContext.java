package com.erp.server.oms.service.impl;

import com.erp.server.oms.service.ShopAuthorizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 店铺授权
 * @Author Luo_WG
 * @Date 2023/10/23 17:52
 **/
@Component
public class ShopAuthorizeContext {

    @Autowired
    private List<ShopAuthorizeService> list;

    public ShopAuthorizeService getBean(Class tclass) {
        for (ShopAuthorizeService strategy : list) {
            if (strategy.getClass() == tclass) {
                return strategy;
            }
        }
        return null;
    }
}
