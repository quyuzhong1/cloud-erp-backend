package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.PickingWaveDetailEntity;
import com.erp.server.wms.mapper.PickingWaveDetailMapper;
import com.erp.server.wms.service.PickingWaveDetailService;

import javax.annotation.Resource;

@Resource
public class PickingWaveDetailServiceImpl extends SuperServiceImpl<PickingWaveDetailMapper, PickingWaveDetailEntity> implements PickingWaveDetailService {
}
