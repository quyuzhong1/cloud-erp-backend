package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.server.workflow.mapper.WorkflowBusinessProcessMapper;
import com.erp.server.workflow.service.WorkflowBusinessProcessService;
import org.springframework.stereotype.Service;

/**
 * @Classname WorkflowBusinessProcessServiceImpl
 * @Description TODO
 * @Date 2023-01-30 15:35
 * @Created by yl
 */
@Service
public class WorkflowBusinessProcessServiceImpl  extends ServiceImpl<WorkflowBusinessProcessMapper, WorkflowBusinessProcessEntity> implements WorkflowBusinessProcessService {
}
