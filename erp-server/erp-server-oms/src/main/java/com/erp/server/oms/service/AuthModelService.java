package com.erp.server.oms.service;


import com.erp.model.oms.dto.ShopAuthorizeDTO;

public interface AuthModelService {
    /**
     * 获取店铺授权地址
     * @param dto
     */
    String getShopAuthorizeUrl(ShopAuthorizeDTO dto);

    /**
     * 店铺授权
     * @param dto
     * @throws Exception
     */
    void shopAuthorize(ShopAuthorizeDTO dto);


    /**
     * 取消授权
     * @param id
     */
    void cleanShopAuthorize(ShopAuthorizeDTO id);
}
