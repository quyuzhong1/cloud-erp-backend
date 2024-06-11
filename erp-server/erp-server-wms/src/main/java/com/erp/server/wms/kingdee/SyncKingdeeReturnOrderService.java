package com.erp.server.wms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.PoReturnEntity;

/**
 * 同步金蝶采购退货单
 * @Author Luo_WG
 * @Date 2023/4/24 11:21
 **/
public interface SyncKingdeeReturnOrderService {

    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    DmpPushTaskEntity syncDataToKingdee(PoReturnEntity entity, String operate);
}
