package com.erp.server.tms.sync;

import java.util.List;
import java.util.Map;

import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;

import cn.hutool.core.lang.Pair;

public interface SyncLogisticsBillService {
    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(LogisticsBillEntity entity,
                                                  LogisticsBillDetailEntity logisticsBillDetailEntity,
                                                  String operate,
                                                  Map<String, Pair<String, String>> logisticInfoMaps);
    
    Map<String, Object> syncNewDataToSdyFieldHandler(LogisticsBillEntity entity,
    		LogisticsBillDetailEntity logisticsBillDetailEntity,
    		String operate,
    		List<LogisticsChannelEntity> logisticsChannelEntities,
    		List<LogisticsSupplierEntity> logisticsSupplierEntities);

    void syncDataToSdy(LogisticsBillEntity entity, List<LogisticsBillDetailEntity> detailEntityList, String operate, Map<String, Pair<String, String>> logisticInfoMaps);

    void syncDataToSdy(LogisticsBillEntity entity, List<LogisticsBillDetailEntity> detailEntityList, String operate);

    Map<String, Pair<String, String>> getLogisticInfo(List<LogisticsBillEntity> entitys);
}
