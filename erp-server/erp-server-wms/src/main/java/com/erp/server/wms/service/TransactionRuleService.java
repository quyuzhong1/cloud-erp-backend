package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.TransactionRuleEntity;

import java.util.List;

/**
 * @Classname: TransactionRuleService
 * @Description: TODO
 * @CreateTime: 2023-04-26  10:20
 * @Author: zhangchunlin
 */
public interface TransactionRuleService  extends SuperService<TransactionRuleEntity> {

    /**
     * 根据业务类型获取库存交易规则
     * @param dictBizType
     * @return
     */
    List<TransactionRuleEntity> findByDictBizType(String dictBizType);

}
