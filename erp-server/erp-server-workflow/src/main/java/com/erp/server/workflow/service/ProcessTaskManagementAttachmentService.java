package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ProcessTaskManagementAttachmentEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ProcessTaskManagementAttachmentDTO;

/**
 * <p>
 * 审核附件表 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-09-05
 */
public interface ProcessTaskManagementAttachmentService extends SuperService<ProcessTaskManagementAttachmentEntity> {

    /**
    * 新增
    * @author tmj
    * @date: 2024-09-05
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProcessTaskManagementAttachmentDTO.AddDTO dto);

    /**
    * 修改
    * @author tmj
    * @date: 2024-09-05
    * @param dto
    * @return
    */
    Boolean update(ProcessTaskManagementAttachmentDTO.UpdateDTO dto);


}
