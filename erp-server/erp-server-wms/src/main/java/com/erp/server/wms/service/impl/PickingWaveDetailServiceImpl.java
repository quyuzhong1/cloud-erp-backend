package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.PickingWaveDetailEntity;
import com.erp.server.wms.mapper.PickingWaveDetailMapper;
import com.erp.server.wms.service.PickingWaveDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PickingWaveDetailServiceImpl extends SuperServiceImpl<PickingWaveDetailMapper, PickingWaveDetailEntity> implements PickingWaveDetailService {
    @Override
    public List<PickingWaveDetailEntity> listByMainId(String mainId) {
        return list(Wrappers.<PickingWaveDetailEntity>lambdaQuery().eq(PickingWaveDetailEntity::getMainId, mainId));
    }
}
