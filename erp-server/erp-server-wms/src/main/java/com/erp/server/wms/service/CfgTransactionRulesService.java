package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.CfgTransactionRulesEntity;

import java.util.List;

/**
 * @Classname: TransactionRuleService
 * @Description: TODO
 * @CreateTime: 2023-04-26  10:20
 * @Author: zhangchunlin
 */
public interface CfgTransactionRulesService extends SuperService<CfgTransactionRulesEntity> {

    /**
     * 根据业务类型获取库存交易规则
     * @param dictBizType
     * @return
     */
    List<CfgTransactionRulesEntity> findByDictBizType(String dictBizType);

}
