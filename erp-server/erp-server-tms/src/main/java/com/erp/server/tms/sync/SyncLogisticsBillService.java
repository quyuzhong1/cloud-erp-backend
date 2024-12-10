package com.erp.server.tms.sync;

import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;

import java.util.List;
import java.util.Map;

public interface SyncLogisticsBillService {
    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(LogisticsBillEntity entity,
                                                  LogisticsBillDetailEntity logisticsBillDetailEntity,
                                                  String operate,
                                                  List<LogisticsChannelEntity> logisticsChannelEntities,
                                                  List<LogisticsSupplierEntity> logisticsSupplierEntities);

    void syncDataToSdy(LogisticsBillEntity entity, List<LogisticsBillDetailEntity> detailEntityList, String operate, List<LogisticsChannelEntity> logisticsChannelEntities, List<LogisticsSupplierEntity> logisticsSupplierEntities);

}
