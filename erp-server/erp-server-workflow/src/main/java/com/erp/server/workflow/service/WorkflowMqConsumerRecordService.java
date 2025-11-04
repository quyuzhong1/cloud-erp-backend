package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.WorkflowMqConsumerRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.WorkflowMqConsumerRecordDTO;

/**
 * <p>
 * mq消费记录 服务类
 * </p>
 *
 * @author jack
 * @since 2025-11-04
 */
public interface WorkflowMqConsumerRecordService extends SuperService<WorkflowMqConsumerRecordEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-11-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WorkflowMqConsumerRecordDTO.AddDTO dto);


}
