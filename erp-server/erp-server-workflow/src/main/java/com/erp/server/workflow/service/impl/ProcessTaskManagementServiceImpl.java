package com.erp.server.workflow.service.impl;

import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.server.workflow.mapper.ProcessTaskManagementMapper;
import com.erp.server.workflow.service.ProcessTaskManagementService;
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
public class ProcessTaskManagementServiceImpl extends SuperServiceImpl<ProcessTaskManagementMapper, ProcessTaskManagementEntity> implements ProcessTaskManagementService {

}
