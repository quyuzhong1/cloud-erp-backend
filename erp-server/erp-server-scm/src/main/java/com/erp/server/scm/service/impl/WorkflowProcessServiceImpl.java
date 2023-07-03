package com.erp.server.scm.service.impl;

import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.scm.service.WorkflowProcessService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: 工作流业务层
 * @date 2023/7/3 15:38
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {


    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        return null;
    }
}
