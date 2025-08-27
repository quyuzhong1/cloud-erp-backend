package com.erp.server.oms.dht;

import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;

public interface SyncDhtService {
    /**
     * 创建推送客户任务
     */
    void createSyncCustomerTaskToDht(CustomerInfoEntity entity, String operate);
    /**
     * 创建推送客户地址任务
     */
    void createSyncCustomerAddressTaskToDht(CustomerAddressEntity entity, String operate);

}
