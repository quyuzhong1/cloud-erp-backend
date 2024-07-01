package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.server.wms.mapper.WaveListPdaMapper;
import com.erp.server.wms.service.WaveListPdaService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @date 2024-07-01
 * @author tanmujin
 */
@Service
public class WaveListPdaServiceImpl extends SuperServiceImpl<WaveListPdaMapper, WaveListEntity> implements WaveListPdaService {
    @Override
    public List<WaveListDetailPdaDTO.ViewDTO> startPicking(String waveId) {
        return Collections.emptyList();
    }
}
