package com.erp.server.wms.service;

import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;

import java.util.List;

public interface SyncB2bThirdWarehouseService {
    /**
     * 创建推送三方仓任务
     */
    void syncB2bThirdWarehouseCreate(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList, String operate);

    /**
     * 取消推送三方仓任务
     * @param entity
     * @param operate
     */
    void syncB2bThirdWarehouseCancel(B2bThirdDeliveryEntity entity, String operate);
}
