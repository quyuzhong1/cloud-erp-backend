package com.erp.server.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.workflow.dto.BusinessTableDTO;
import com.erp.model.workflow.dto.WorkflowBusinessProcessDTO;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;

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

    /**
     * 获取到当前审核人
     * @author yl
     * @date 2023-02-13 9:39
     * @param businessTableIds
     * @return java.util.List<com.erp.model.workflow.vo.ProcessCurrentAuditorVO>
     */
    List<ProcessCurrentAuditorVO> getProcessCurrentAuditor(List<String> businessTableIds);

    List<WorkflowBusinessProcessDTO> getProcessByTables(List<String> businessTableIds);
}
