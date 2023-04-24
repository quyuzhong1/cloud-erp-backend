package com.erp.server.wms.kingdee.impl;

import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import com.erp.server.wms.kingdee.SyncKingdeeReturnOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 同步金蝶采购退货单
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeReturnOrderServiceImpl implements SyncKingdeeReturnOrderService {
    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    @Override
    public void syncDataToKingdee(PurchaseReturnOrderEntity entity, String operate) {


    }
}
