package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.PickingWaveDetailEntity;
import com.erp.server.wms.mapper.PickingWaveDetailMapper;
import com.erp.server.wms.service.PickingWaveDetailService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PickingWaveDetailServiceImpl extends SuperServiceImpl<PickingWaveDetailMapper, PickingWaveDetailEntity> implements PickingWaveDetailService {
    @Override
    public List<PickingWaveDetailEntity> listByMainId(String mainId) {
        return list(Wrappers.<PickingWaveDetailEntity>lambdaQuery().eq(PickingWaveDetailEntity::getMainId, mainId));
    }

    @Override
    public Map<String, String> getOrderBasketNoMap(List<String> soIds) {
        List<PickingWaveDetailEntity> pickingWaveDetailEntityList =list(Wrappers.<PickingWaveDetailEntity>lambdaQuery().in(PickingWaveDetailEntity::getSoId, soIds));
        return pickingWaveDetailEntityList.stream().collect(Collectors.toMap(PickingWaveDetailEntity::getSoId, PickingWaveDetailEntity::getBasketNo, (k1, k2)->k1));
    }
}
