package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.server.wms.mapper.WaveListDetailMapper;
import com.erp.server.wms.service.WaveListDetailService;
import org.springframework.stereotype.Service;

/**
 * 波次详情服务类
 * @date 2024-07-01
 * @author tanmujin
 */
@Service
public class WaveListDetailServiceImpl extends SuperServiceImpl<WaveListDetailMapper, WaveListDetailEntity> implements WaveListDetailService {
}
