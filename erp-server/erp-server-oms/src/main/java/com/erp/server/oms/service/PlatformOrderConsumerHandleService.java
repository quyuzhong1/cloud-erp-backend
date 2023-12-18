package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;

/**
 * 平台消费处理接口
 * @param
 * @author Jim
 * @date 2023/12/18
 * @Return
 */
public interface PlatformOrderConsumerHandleService {

    /**
     * 消费处理所有
     * @param dto
     */
    void handleAll(PlatformOrderDTO dto);
}
