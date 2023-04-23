package com.erp.server.workflow.service.impl;

import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.server.workflow.mapper.ProcessDefinitionMapper;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class ProcessDefinitionServiceImpl extends SuperServiceImpl<ProcessDefinitionMapper, ProcessDefinitionEntity> implements ProcessDefinitionService {

}
