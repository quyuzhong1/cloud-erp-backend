package com.erp.server.wms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;

public interface SyncKingdeePoReceiveService {
    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    DmpPushTaskEntity syncDataToKingdee(WarehouseReceiveEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(WarehouseReceiveEntity entity, String operate);
}
