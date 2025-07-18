package com.erp.server.workflow.service;
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementExtEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * process_task_management拓展表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface ProcessTaskManagementExtService extends SuperService<ProcessTaskManagementExtEntity> {


    List<ProcessTaskManagementExtDTO.MessageDTO> listMessageIdByTaskIds(List<String> processTaskManagementIds);

    List<ProcessTaskManagementEntity> listProcessTaskByTaskIds(List<String> processTaskManagementIds,String processInstanceId);
}
