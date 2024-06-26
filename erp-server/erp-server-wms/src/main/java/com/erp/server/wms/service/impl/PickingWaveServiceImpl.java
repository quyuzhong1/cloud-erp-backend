package com.erp.server.wms.service.impl;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.PickingWaveEntity;
import com.erp.server.wms.mapper.PickingWaveMapper;
import com.erp.server.wms.service.PickingWaveService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PickingWaveServiceImpl extends SuperServiceImpl<PickingWaveMapper, PickingWaveEntity> implements PickingWaveService {
    @Override
    public int countDelivery(PermissionsDTO param) {
        // todo 需要状态字段值
        return 0;
    }

    @Override
    public List<String> listDeliveryIdByStatus(String status) {
        return baseMapper.listDeliveryIdByStatus(status);
    }
}
