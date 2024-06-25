package com.erp.server.wms.rocketmq.sync;

import com.common.business.dto.WdtReturnOrderDTO;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;

public interface SyncSoReturnService {

    /**
     * 同步金蝶退货单到OMS退货订单
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncKingdeeReturnOrderToSoReturn(KingdeeReturnOrderEntity list);


    /**
     *  同步旺店通退货入库单
     * @param dto 退货入库单
     */
    void syncWdtReturnOrderToSoReturn(WdtReturnOrderDTO dto);

}
