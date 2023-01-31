package com.erp.server.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.workflow.dto.FindProcessDTO;
import com.erp.model.workflow.dto.WorkflowBusinessDTO;
import com.erp.model.workflow.entity.WorkflowBusinessEntity;
import com.erp.model.workflow.vo.WorkflowBusinessVO;

import java.util.List;

/**
 * @Classname WorkflowBusinessService
 * @Description TODO
 * @Date 2023-01-30 15:29
 * @Created by yl
 */
public interface WorkflowBusinessService  extends IService<WorkflowBusinessEntity> {
    List<WorkflowBusinessVO> getBusinessList(FindProcessDTO  dto);

    Boolean saveBusiness(WorkflowBusinessDTO dto);
}
