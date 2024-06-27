package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseResultDTO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.dto.renovation.PickingWaveDTO;
import com.erp.model.wms.entity.PickingWaveEntity;
import com.erp.server.wms.mapper.PickingWaveMapper;
import com.erp.server.wms.service.PickingWaveService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PickingWaveServiceImpl extends SuperServiceImpl<PickingWaveMapper, PickingWaveEntity> implements PickingWaveService {

    @Override
    public BaseResultDTO.AddDTO add(PickingWaveDTO.AddDTO dto) {
        return null;
    }

    @Override
    public int countDelivery(PermissionsDTO param) {
        // todo 需要状态字段值
        return baseMapper.countDelivery(param);
    }

    @Override
    public List<String> listDeliveryIdByStatus(String status) {
        return baseMapper.listDeliveryIdByStatus(status);
    }

    @Override
    public PickingWaveEntity getByCodeOrCarCode(String code) {
        return getOne(Wrappers.<PickingWaveEntity>lambdaQuery().eq(PickingWaveEntity::getCode, code).or().eq(PickingWaveEntity::getPickingCartCode, code));
    }

    @Override
    public List<PickingWaveDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId) {
        return listDetailByMainId(waveId, null);
    }

    @Override
    public List<PickingWaveDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String skuId) {
        return listDetailByMainId(waveId, null, skuId);
    }

    @Override
    public List<PickingWaveDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String basketNo, String skuId) {
        return baseMapper.listDetailByMainId(waveId, basketNo, skuId);
    }

    @Override
    public PickingWaveEntity getByCode(String code) {
        return getOne(Wrappers.<PickingWaveEntity>lambdaQuery().eq(PickingWaveEntity::getCode, code).last("limit 1"));
    }

    @Override
    public List<PickingWaveEntity> listByCarCode(String carCode) {
        return list(Wrappers.<PickingWaveEntity>lambdaQuery().eq(PickingWaveEntity::getPickingCartCode, carCode));
    }
}
