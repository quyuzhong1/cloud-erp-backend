package com.erp.server.sys.mapper;
import com.erp.model.sys.entity.MqConsumerRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * mq消费记录 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-05-29
 */
@Mapper
public interface MqConsumerRecordMapper extends BaseMapper<MqConsumerRecordEntity> {

}
