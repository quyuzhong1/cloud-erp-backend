package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.CfgTransactionRulesEntity;
import com.erp.server.wms.mapper.CfgTransactionRulesMapper;
import com.erp.server.wms.service.CfgTransactionRulesService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname: TransactionRuleServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-26  10:20
 * @Author: zhangchunlin
 */
@Service
public class CfgTransactionRulesServiceImpl extends SuperServiceImpl<CfgTransactionRulesMapper, CfgTransactionRulesEntity> implements CfgTransactionRulesService {

    @Override
    public List<CfgTransactionRulesEntity> findByDictBizType(String dictBizType) {
        return lambdaQuery().eq(CfgTransactionRulesEntity::getDictBizType,dictBizType).list();
    }

}