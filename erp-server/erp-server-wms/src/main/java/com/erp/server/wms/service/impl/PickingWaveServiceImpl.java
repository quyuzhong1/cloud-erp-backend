package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.PickingWaveEntity;
import com.erp.server.wms.mapper.PickingWaveMapper;
import com.erp.server.wms.service.PickingWaveService;

import javax.annotation.Resource;

@Resource
public class PickingWaveServiceImpl extends SuperServiceImpl<PickingWaveMapper, PickingWaveEntity> implements PickingWaveService {
}
