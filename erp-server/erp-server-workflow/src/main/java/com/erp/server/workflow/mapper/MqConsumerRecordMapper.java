package com.erp.server.workflow.mapper;
import com.erp.model.workflow.entity.MqConsumerRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * mq消费记录 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-11-04
 */
@Mapper
public interface MqConsumerRecordMapper extends BaseMapper<MqConsumerRecordEntity> {
    /**
     * 插入 MQ 消费记录
     * @param record MQ 消费记录实体
     * @return 插入行数
     */
    int insertMqConsumerRecord(@Param("record") MqConsumerRecordEntity record);
}
