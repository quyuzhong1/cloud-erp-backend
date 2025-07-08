package com.erp.server.sys.service;
import com.erp.model.sys.entity.MqConsumerRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.MqConsumerRecordDTO;

/**
 * <p>
 * mq消费记录 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-29
 */
public interface MqConsumerRecordService extends SuperService<MqConsumerRecordEntity> {


    String addMqRecord(MqConsumerRecordDTO.MqDTO dto);
}
