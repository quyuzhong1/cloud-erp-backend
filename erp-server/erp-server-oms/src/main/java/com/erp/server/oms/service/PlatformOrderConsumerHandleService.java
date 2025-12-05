package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

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


    void handleRule(SoB2cEntity mainEntity);

    /**
     * 保存所有
     */

    SoB2cDTO.PullOrderResultDTO checkAndSaveAll(PlatformOrderDTO dto);

    Boolean tiktokSplit(PlatformOrderDTO dto);

    void updateTikTokDetail(PlatformOrderDTO dto);

    List<PlatformOrderDTO> handleMercadolibre(PlatformOrderDTO dto);
}
