package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.renovation.PickingWaveDTO;
import com.erp.model.wms.entity.WaveListEntity;

/**
 * 
 * @date 2024-07-01
 * @author tanmujin
 */
public interface WaveListFeignService extends SuperService<WaveListEntity> {
    /**
     * 新增波次
     */
    BatchResultDTO add(PickingWaveDTO.AddDTO addDto);
}
