package com.erp.server.workflow.service.impl;


import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.WorkflowMqConsumerRecordEntity;
import com.erp.server.workflow.mapper.WorkflowMqConsumerRecordMapper;
import com.erp.server.workflow.service.WorkflowMqConsumerRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.WorkflowMqConsumerRecordDTO;
import com.common.core.utils.*;
/**
 * <p>
 * mq消费记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-11-04
 */
@Slf4j
@Service
public class WorkflowMqConsumerRecordServiceImpl extends SuperServiceImpl<WorkflowMqConsumerRecordMapper, WorkflowMqConsumerRecordEntity> implements WorkflowMqConsumerRecordService {


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WorkflowMqConsumerRecordDTO.AddDTO addDTO) {
        WorkflowMqConsumerRecordEntity mqConsumerRecordEntity = new WorkflowMqConsumerRecordEntity();
        BeanMapperUtils.copy(addDTO, mqConsumerRecordEntity);
        log.info("开始新增mq消费记录");
        boolean save = super.save(mqConsumerRecordEntity);
        if(!save) {
            throw new ServiceException("mq消费记录保存失败");
        }

        return new BaseResultDTO.AddDTO(mqConsumerRecordEntity.getId(), mqConsumerRecordEntity.getId());
    }

}
