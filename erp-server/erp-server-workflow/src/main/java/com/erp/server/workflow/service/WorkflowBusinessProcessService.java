package com.erp.server.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;

import java.util.List;

/**
 * @Classname WorkflowBusinessProcessService
 * @Description TODO
 * @Date 2023-01-30 15:29
 * @Created by yl
 */
public interface WorkflowBusinessProcessService extends IService<WorkflowBusinessProcessEntity> {
    WorkflowBusinessProcessEntity getByProcessId(String processId);

    List<WorkflowBusinessProcessEntity> getByProcessIds(List<String> processIds);
}
