package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.dto.WaveListPdaDTO;
import com.erp.model.wms.entity.WaveListEntity;

import java.util.List;

/**
 * 波次列表（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
public interface WaveListPdaService extends SuperService<WaveListEntity> {
//    List<WaveListPdaDTO.ViewDTO> list();

    List<WaveListDetailPdaDTO.ViewDTO> startPicking(String waveId);
}
