package com.erp.server.wms.service;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;

import java.util.List;
import java.util.Map;

public interface SyncB2bThirdWarehouseService {
    /**
     * 新增同步数据到三方仓
     * @param entity
     * @param detailEntityList
     * @return
     */
    Map<String, Object> newSyncDataToThirdWarehouseCreate(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList);

    /**
     * 创建推送三方仓任务
     */
    DmpPushTaskEntity syncB2bThirdWarehouse(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList, String operate);

    /**
     * 取消推送三方仓任务
     * @param entity
     */
    Map<String, Object> newSyncDataToThirdWarehouseCancel(B2bThirdDeliveryEntity entity);
}
