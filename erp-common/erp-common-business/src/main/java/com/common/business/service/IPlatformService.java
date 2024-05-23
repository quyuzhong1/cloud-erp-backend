package com.common.business.service;

import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;

import java.util.List;

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
     * 订单取消后发货单拦截
     * @param dto
     * @return
     */
    Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto);


    /**
     * 查询并更新平台订单状态
     */
    Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto);

    /**
     * 批量异常查询并更新平台订单状态
     */
    Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList);
}
