package com.erp.server.workflow.service;

import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
public interface ProcessBusinessService extends SuperService<ProcessBusinessEntity> {

    /**
     * 根据流程定义ID获取流程业务
     * @param definitionId
     * @return
     */
    ProcessBusinessEntity getByDefinitionId(String definitionId);


    /**
     * 根据流程定义IDs获取流程业务
     * @param definitionIds
     * @return
     */
    List<ProcessBusinessEntity> getByDefinitionIds(List<String> definitionIds);

    /**
     * 根据业务ID获取流程业务
     * @param businessKey
     * @return
     */
    ProcessBusinessEntity getProcessBusiness(String businessKey, String condition);

    /**
     * 保存流程业务绑定关系
     * @param dto
     */
    void addOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto);
}
