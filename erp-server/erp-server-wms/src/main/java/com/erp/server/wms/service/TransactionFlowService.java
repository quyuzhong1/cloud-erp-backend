package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.TransactionFlowEntity;

import java.util.List;

/**
 * @Classname: TransactionFlowService
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
public interface TransactionFlowService extends SuperService<TransactionFlowEntity> {

    /**
     * 根据单据来源和单据id查询出库存交易流水
     * @param sourceType
     * @param sourceId
     * @return
     */
    List<TransactionFlowEntity> getTxnFlowSCreatTimeSorted(String sourceType, String sourceId);

}
