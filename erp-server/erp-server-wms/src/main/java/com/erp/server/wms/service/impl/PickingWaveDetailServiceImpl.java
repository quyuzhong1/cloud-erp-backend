package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.server.wms.mapper.WaveListDetailMapper;
import com.erp.server.wms.service.PickingWaveDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PickingWaveDetailServiceImpl extends SuperServiceImpl<WaveListDetailMapper, WaveListDetailEntity> implements PickingWaveDetailService {
    @Override
    public List<WaveListDetailEntity> listByMainId(String mainId) {
        return list(Wrappers.<WaveListDetailEntity>lambdaQuery().eq(WaveListDetailEntity::getMainId, mainId));
    }

    @Override
    public Map<String, String> getOrderBasketNoMap(List<String> soIds) {
        List<WaveListDetailEntity> waveListDetailEntityList =list(Wrappers.<WaveListDetailEntity>lambdaQuery().in(WaveListDetailEntity::getSoId, soIds));
        return waveListDetailEntityList.stream().collect(Collectors.toMap(WaveListDetailEntity::getSoId, WaveListDetailEntity::getBasketNo, (k1, k2)->k1));
    }
}
