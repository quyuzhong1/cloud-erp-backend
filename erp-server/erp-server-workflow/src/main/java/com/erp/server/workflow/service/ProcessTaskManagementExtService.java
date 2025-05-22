package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ProcessTaskManagementExtEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;

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


    List<String> listByProcessTaskManagementIds(List<String> processTaskManagementIds);
}
