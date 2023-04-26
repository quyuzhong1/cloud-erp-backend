package com.erp.server.workflow.service.impl;

import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.server.workflow.mapper.ProcessBusinessMapper;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class ProcessBusinessServiceImpl extends SuperServiceImpl<ProcessBusinessMapper, ProcessBusinessEntity> implements ProcessBusinessService {

    @Override
    public List<ProcessBusinessEntity> getByDefinitionId(String definitionId) {
        List<ProcessBusinessEntity> list = lambdaQuery()
                .eq(ProcessBusinessEntity::getProcessDefinitionId, definitionId)
                .list();
        return list;
    }

    @Override
    public List<ProcessBusinessEntity> getByDefinitionIds(List<String> definitionIds) {
        List<ProcessBusinessEntity> list = lambdaQuery()
                .in(ProcessBusinessEntity::getProcessDefinitionId, definitionIds)
                .list();
        return list;
    }

    @Override
    public ProcessBusinessEntity getProcessBusiness(String businessKey) {
        ProcessBusinessEntity processBusiness = lambdaQuery()
                .eq(ProcessBusinessEntity::getBusinessKey, businessKey)
                .eq(ProcessBusinessEntity::getDisabled, Boolean.FALSE)
                .one();
        return processBusiness;
    }
}
