package com.erp.server.wms.service;

import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;

public interface ShipOrderModeService {

    /**
     * 平台发货
     * @param dto
     * @return
     */
    Boolean platformShipOrder(ShopAuthorizeUrlDTO dto);
}
