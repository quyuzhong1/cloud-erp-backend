package com.erp.server.oms.rocketmq.sync.wangdian;


import com.erp.model.oms.entity.SoInfoEntity;

public interface SyncWangDianDeliveryService {

    void syncDataToWangDian(SoInfoEntity soInfoEntity);

}
