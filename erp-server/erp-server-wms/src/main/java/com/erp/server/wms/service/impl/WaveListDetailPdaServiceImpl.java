package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.server.wms.mapper.WaveListDetailPdaMapper;
import com.erp.server.wms.service.WaveListDetailPdaService;
import org.springframework.stereotype.Service;

/**
 * 波次详情（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@Service
public class WaveListDetailPdaServiceImpl extends SuperServiceImpl<WaveListDetailPdaMapper, WaveListDetailEntity> implements WaveListDetailPdaService {
}
