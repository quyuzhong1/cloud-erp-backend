package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ProcessTaskManagementExtEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;

/**
 * <p>
 * process_task_management拓展表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface ProcessTaskManagementExtService extends SuperService<ProcessTaskManagementExtEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProcessTaskManagementExtDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(ProcessTaskManagementExtDTO.UpdateDTO dto);


}
