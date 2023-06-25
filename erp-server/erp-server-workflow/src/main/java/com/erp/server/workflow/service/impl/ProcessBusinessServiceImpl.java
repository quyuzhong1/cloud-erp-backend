package com.erp.server.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
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
    public ProcessBusinessEntity getByDefinitionId(String definitionId) {
        ProcessBusinessEntity entity = lambdaQuery()
                .eq(ProcessBusinessEntity::getDisabled, Boolean.FALSE)
                .eq(ProcessBusinessEntity::getProcessDefinitionId, definitionId)
                .last("limit 1")
                .one();
        return entity;
    }

    @Override
    public List<ProcessBusinessEntity> getByDefinitionIds(List<String> definitionIds) {
        List<ProcessBusinessEntity> list = lambdaQuery()
                .in(ProcessBusinessEntity::getProcessDefinitionId, definitionIds)
                .list();
        return list;
    }

    @Override
    public ProcessBusinessEntity getProcessBusiness(String businessKey, String condition) {
        ProcessBusinessEntity processBusiness = lambdaQuery()
                .eq(ProcessBusinessEntity::getBusinessKey, businessKey)
                .eq(StrUtil.isNotBlank(condition), ProcessBusinessEntity::getStartCondition, condition)
                .eq(ProcessBusinessEntity::getDisabled, Boolean.FALSE)
                .orderByDesc(ProcessBusinessEntity::getUpdateTime)
                .last("limit 1")
                .one();
        return processBusiness;
    }

    @Override
    public void addOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        ProcessBusinessEntity business = getProcessBusiness(dto.getBusinessKey(),"");
        if(business != null) {
            throw new ServiceException(ApiError.PROCESS_BUSINESS_KEY_EXIST);
        }
        ProcessBusinessEntity oldBusinessEntity = getByDefinitionId(dto.getId());
        if(null != oldBusinessEntity){
            oldBusinessEntity.setBusinessKey(dto.getBusinessKey());
            if(!updateById(oldBusinessEntity)){
                throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
            }
        }else {
            ProcessBusinessEntity processBusinessEntity = new ProcessBusinessEntity(dto);
            if(!save(processBusinessEntity)){
                throw new ServiceException(ApiError.SAVE_PROCESS_ERROR);
            }
        }
    }
}
