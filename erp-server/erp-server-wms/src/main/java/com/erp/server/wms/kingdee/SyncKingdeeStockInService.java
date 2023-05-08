package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.PoInstockEntity;

public interface SyncKingdeeStockInService {

    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    void syncDataToKingdee(PoInstockEntity entity, String operate);
}
