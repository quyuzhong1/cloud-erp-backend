package com.erp.server.wms.kingdee;

import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    void syncDataToKingdee(PurchaseReturnOrderEntity entity, String operate);
}
