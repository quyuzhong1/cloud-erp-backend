package com.erp.server.workflow.mapper;
import com.erp.model.workflow.entity.WorkflowMqConsumerRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * mq消费记录 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-11-04
 */
@Mapper
public interface WorkflowMqConsumerRecordMapper extends BaseMapper<WorkflowMqConsumerRecordEntity> {

}
