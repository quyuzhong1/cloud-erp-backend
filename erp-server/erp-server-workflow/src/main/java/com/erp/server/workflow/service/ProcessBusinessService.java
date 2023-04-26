package com.erp.server.workflow.service;

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
    List<ProcessBusinessEntity> getByDefinitionId(String definitionId);


    /**
     * 根据流程定义IDs获取流程业务
     * @param definitionIds
     * @return
     */
    List<ProcessBusinessEntity> getByDefinitionIds(List<String> definitionIds);
}
