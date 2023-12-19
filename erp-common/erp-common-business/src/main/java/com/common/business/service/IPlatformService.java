package com.common.business.service;

import com.common.business.dto.PlatformShipOrderDTO;

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
    String shipOrder(PlatformShipOrderDTO dto);

}
