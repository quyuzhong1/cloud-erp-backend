package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.TransactionRuleEntity;
import com.erp.server.wms.mapper.TransactionRuleMapper;
import com.erp.server.wms.service.TransactionRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname: TransactionRuleServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-26  10:20
 * @Author: zhangchunlin
 */
@Service
public class TransactionRuleServiceImpl extends SuperServiceImpl<TransactionRuleMapper, TransactionRuleEntity> implements TransactionRuleService {

    @Override
    public List<TransactionRuleEntity> findByDictBizType(String dictBizType) {
        return lambdaQuery().eq(TransactionRuleEntity::getDictBizType,dictBizType).list();
    }

}