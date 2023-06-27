package com.erp.server.oms.rocketmq.sync.oms.impl;

import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.server.oms.rocketmq.sync.oms.SyncSoReturnService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncSoReturnServiceImpl implements SyncSoReturnService {

    @Override
    public void syncKingdeeReturnOrderToSoReturn(List<KingdeeReturnOrderEntity> list) {
        
    }
}
