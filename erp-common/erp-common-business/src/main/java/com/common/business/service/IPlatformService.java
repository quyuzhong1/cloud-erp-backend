package com.common.business.service;

import com.common.business.dto.PlatformShipOrderDTO;

/**
 * 平台发货
 * @Author Luo_WG
 * @Date 2023/12/18 15:53
 **/
public interface IPlatformService {

    /**
     * 订单发货标识
     * @param dto
     * @return
     */
    void shipOrder(PlatformShipOrderDTO dto);

    /**
     * 订单发货获取标发单号类型
     * @param platform
     * @param logisticsChannelId
     * @return
     */
    String getStandardOrderType(String platform,String logisticsChannelId);
}
