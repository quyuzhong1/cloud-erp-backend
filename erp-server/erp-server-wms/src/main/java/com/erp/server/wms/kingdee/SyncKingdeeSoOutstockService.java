package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.PoInstockEntity;

/**
 * 销售出库单
 * @Author Luo_WG
 * @Date 2023/5/31 16:41
 **/
public interface SyncKingdeeSoOutstockService {
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
