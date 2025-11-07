package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.MqConsumerRecordEntity;
import com.common.business.service.SuperService;
import com.erp.model.workflow.dto.WorkflowMqConsumerRecordDTO;

/**
 * <p>
 * mq消费记录 服务类
 * </p>
 *
 * @author jack
 * @since 2025-11-04
 */
public interface MqConsumerRecordService extends SuperService<MqConsumerRecordEntity> {


    String addMqRecord(WorkflowMqConsumerRecordDTO.MqDTO dto);
}
