package com.erp.server.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.workflow.dto.BusinessTableDTO;
import com.erp.model.workflow.dto.WorkflowBusinessProcessDTO;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.model.workflow.vo.MyToDoTaskVO;

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

    Boolean saveBusinessProcess(WorkflowBusinessProcessDTO dto);

    MyToDoTaskVO getProcessByBusinessTable(BusinessTableDTO dto);
}
