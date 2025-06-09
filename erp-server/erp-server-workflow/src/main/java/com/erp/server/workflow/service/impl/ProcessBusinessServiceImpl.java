package com.erp.server.workflow.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.constant.SqlConstants;
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
        return lambdaQuery()
                .eq(ProcessBusinessEntity::getDisabled, Boolean.FALSE)
                .eq(ProcessBusinessEntity::getProcessDefinitionId, definitionId)
                .last(SqlConstants.LIMIT_1)
                .one();
    }

    @Override
    public List<ProcessBusinessEntity> getByDefinitionIds(List<String> definitionIds) {
        return lambdaQuery()
                .in(ProcessBusinessEntity::getProcessDefinitionId, definitionIds)
                .list();
    }

    @Override
    public ProcessBusinessEntity getProcessBusiness(String businessKey, String condition, Boolean disabled) {
        return lambdaQuery()
                .eq(ProcessBusinessEntity::getBusinessKey, businessKey)
                .eq(null != condition, ProcessBusinessEntity::getStartCondition, condition)
                .eq(null != disabled, ProcessBusinessEntity::getDisabled, disabled)
                .orderByDesc(ProcessBusinessEntity::getUpdateTime)
                .last(SqlConstants.LIMIT_1)
                .one();
    }

    @Override
    public void addOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto, Boolean isSave) {

        ProcessBusinessEntity entity = CharSequenceUtil.isNotBlank(dto.getBusinessId()) ? getById(dto.getBusinessId()) : null;
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
