package com.erp.server.wms.service;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.PickingWaveEntity;

import java.util.List;

public interface PickingWaveService extends SuperService<PickingWaveEntity> {
    /**
     * 统计发货批次的数量
     */
    int countDelivery(PermissionsDTO param);

    /**
     * 根据状态获取
     * @param status
     * @return
     */
    List<String> listDeliveryIdByStatus(String status);
}
