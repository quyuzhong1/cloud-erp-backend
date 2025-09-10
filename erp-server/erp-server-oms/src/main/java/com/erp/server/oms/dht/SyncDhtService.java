package com.erp.server.oms.dht;

import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerCreditApplyEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoReceiptEntity;

public interface SyncDhtService {
    /**
     * 创建推送客户任务
     */
    void createSyncCustomerTaskToDht(CustomerInfoEntity entity, String operate);
    /**
     * 创建推送客户地址任务
     */
    void createSyncCustomerAddressTaskToDht(CustomerAddressEntity entity, String operate);

    /**
     * 创建推送客户授信任务
     * @author will
     * @date 2025/9/5 17:32
     * @param entity
     * @param operate
     * @return void
     */
    void createSyncCustomerCreditApplyTaskToDht(CustomerCreditApplyEntity entity, String operate);
    /**
     * 创建推送收款单任务
     */
    void createSyncReceiptTaskToDht(SoReceiptEntity soReceiptEntity, String operate);
}
