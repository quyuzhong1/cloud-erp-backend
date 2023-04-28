package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.server.wms.mapper.TransactionFlowMapper;
import com.erp.server.wms.service.TransactionFlowService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname: TransactionFlowServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
@Service
public class TransactionFlowServiceImpl extends SuperServiceImpl<TransactionFlowMapper, TransactionFlowEntity> implements TransactionFlowService {

    @Override
    public List<TransactionFlowEntity> getTxnFlowSCreatTimeSorted(String sourceType, String sourceId) {
        List<TransactionFlowEntity> txnFlows =  lambdaQuery().eq(TransactionFlowEntity::getSourceType, sourceType)
                .eq(TransactionFlowEntity::getSourceId, sourceId).list();
        if(CollUtil.isNotEmpty(txnFlows)) {
            txnFlows = txnFlows.stream().sorted(Comparator.comparing(TransactionFlowEntity::getCreateTime)).collect(Collectors.toList());
        }
        return txnFlows;
    }

}