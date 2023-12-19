package com.erp.server.wms.service;

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
     * 订单发货标识
     * @param dto
     * @return
     */
    String shipOrder(ShopAuthorizeUrlDTO dto);

}
