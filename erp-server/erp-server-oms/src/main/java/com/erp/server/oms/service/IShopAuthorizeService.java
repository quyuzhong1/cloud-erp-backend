package com.erp.server.oms.service;

import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;

/**
 * 平台授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:56
 **/
public interface IShopAuthorizeService<T> {

    /**
     * 获取平台授权地址
     * @param dto
     * @return
     */
    String getShopAuthorizeUrl(ShopAuthorizeDTO dto);

    /**
     * 授权
     * @param dto
     */
    Boolean shopAuthorize(ShopAuthorizeDTO dto);

    /**
     * 取消授权
     * @param dto
     */
    Boolean cancelAuthorize(CancelAuthorizeDTO dto);

}
