package com.erp.server.oms.service;


import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;

public interface AuthModelService {
    /**
     * 获取店铺授权地址
     * @param dto
     */
    String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto);

    /**
     * 店铺授权
     * @param dto
     * @throws Exception
     */
    Boolean shopAuthorize(ShopAuthorizeDTO dto);


    /**
     * 取消授权
     * @param dto
     */
    Boolean cancelAuthorize(CancelAuthorizeDTO dto);
}
