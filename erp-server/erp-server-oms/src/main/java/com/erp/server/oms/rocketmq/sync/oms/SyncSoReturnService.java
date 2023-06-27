package com.erp.server.oms.rocketmq.sync.oms;

import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.plm.entity.ProductInfoEntity;

import java.util.List;

public interface SyncSoReturnService {

    /**
     * 同步金蝶退货单到OMS退货订单
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncKingdeeReturnOrderToSoReturn(List<KingdeeReturnOrderEntity> list);
}
