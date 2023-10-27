package com.erp.server.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.server.workflow.mapper.ProcessBusinessMapper;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.common.business.service.impl.SuperServiceImpl;
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
    public ProcessBusinessEntity getProcessBusiness(String businessKey, String condition, Boolean disabled) {
        ProcessBusinessEntity processBusiness = lambdaQuery()
                .eq(ProcessBusinessEntity::getBusinessKey, businessKey)
                .eq(null != condition, ProcessBusinessEntity::getStartCondition, condition)
                .eq(null != disabled, ProcessBusinessEntity::getDisabled, disabled)
                .orderByDesc(ProcessBusinessEntity::getUpdateTime)
                .last("limit 1")
                .one();
        return processBusiness;
    }

    @Override
    public void addOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto, Boolean isSave) {
//        ProcessBusinessEntity oldBusinessEntity = getByDefinitionId(dto.getId());

        ProcessBusinessEntity entity = StrUtil.isNotBlank(dto.getBusinessId()) ? getById(dto.getBusinessId()) : null;
        ProcessBusinessEntity oldBusinessEntity = getProcessBusiness(dto.getBusinessKey(),"", null);
        if(null != entity){
            if(null != oldBusinessEntity && !oldBusinessEntity.getId().equals(entity.getId())) {
                throw new ServiceException(ApiError.PROCESS_BUSINESS_KEY_EXIST);
            }
            entity.setBusinessKey(dto.getBusinessKey());
            entity.setProcessDefinitionId(dto.getId());
            if(!updateById(entity)){
                throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
            }
        }else {
            if(null != oldBusinessEntity) {
                throw new ServiceException(ApiError.PROCESS_BUSINESS_KEY_EXIST);
            }
            ProcessBusinessEntity processBusinessEntity = new ProcessBusinessEntity(dto);
            if(!save(processBusinessEntity)){
                throw new ServiceException(ApiError.SAVE_PROCESS_ERROR);
            }
        }
    }
}
