package com.erp.server.wms.sdk;

import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;

/**
 * 平台发货
 * @Author Luo_WG
 * @Date 2023/12/18 15:53
 **/
public interface IPlatformService<T> {

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
    Boolean shopAuthorize(ShopAuthorizeDTO dto);

    /**
     * 取消授权
     * @param dto
     */
    Boolean cancelAuthorize(CancelAuthorizeDTO dto);

}
