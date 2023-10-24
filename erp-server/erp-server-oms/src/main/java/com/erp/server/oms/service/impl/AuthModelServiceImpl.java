package com.erp.server.oms.service.impl;


import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.server.oms.service.AuthModelService;
import org.springframework.stereotype.Service;

/**
 * 授权模板模式
 * @Author Luo_WG
 * @Date 2023/10/23 18:49
 **/
@Service
public class AuthModelServiceImpl implements AuthModelService {

    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeDTO dto) {
        return AuthSaveHandler.getShopAuthorizeUrl(dto);
    }

    @Override
    public void shopAuthorize(ShopAuthorizeDTO dto) {
        AuthSaveHandler.shopAuthorize(dto);
    }

    @Override
    public void cleanShopAuthorize(ShopAuthorizeDTO dto) {
        AuthSaveHandler.cleanShopAuthorize(dto);
    }
}
