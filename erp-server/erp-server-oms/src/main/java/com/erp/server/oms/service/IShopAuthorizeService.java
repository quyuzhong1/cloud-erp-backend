package com.erp.server.oms.service;

import com.erp.model.oms.dto.*;

import javax.servlet.http.HttpServletResponse;

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
    String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto);

    /**
     * 授权
     * @param dto
     */
    Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response);

    /**
     * 取消授权
     * @param dto
     */
    Boolean cancelAuthorize(CancelAuthorizeDTO dto);

    /**
     * 刷新token
     * @param dto
     */
    Boolean refreshToken(RefreshShopTokenDTO dto);

}
