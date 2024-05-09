package com.erp.server.oms.service;

import com.common.business.dto.PlatformDeliveryInterceptDTO;

import java.util.List;

/**
 * <p>
 * B2C销售订单表 状态服务类
 * </p>
 *
 */
public interface SoB2cStatusService{

    /**
     * 更新平台订单取消状态
     */
    Boolean updateCancelAndLog(PlatformDeliveryInterceptDTO dto);


    /**
     * 批量更新平台订单取消状态
     */
    Boolean batchUpdateCancelAndLog(List<String> soB2cIdList);

}
