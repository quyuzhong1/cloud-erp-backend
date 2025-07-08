package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.utils.ApplicationContextUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.sys.entity.MqConsumerRecordEntity;
import com.erp.server.sys.mapper.MqConsumerRecordMapper;
import com.erp.server.sys.service.MqConsumerRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * mq消费记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-29
 */
@Slf4j
@Service
public class MqConsumerRecordServiceImpl extends SuperServiceImpl<MqConsumerRecordMapper, MqConsumerRecordEntity> implements MqConsumerRecordService {

    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    /**
     * 新增一条mq的消费记录
     */
    @Override
    public String addMqRecord(MqConsumerRecordDTO.MqDTO dto) {
        Gson gson = new Gson();
        MqConsumerRecordEntity mqConsumerRecord = new MqConsumerRecordEntity();
        mqConsumerRecord.setTopic(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC.replace("${spring.cloud.nacos.discovery.namespace}", namespace));
        mqConsumerRecord.setTag(RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.name());
        mqConsumerRecord.setConsumerGroup(RocketMqConsumerGroup.SYS_SEND_THIRD_NOTICE_CONSUMER.replace("${spring.cloud.nacos.discovery.namespace}", namespace));
        mqConsumerRecord.setDataJson(gson.toJson(dto.getDataJson()));
        boolean flag = ApplicationContextUtils.getBean(MqConsumerRecordServiceImpl.class).save(mqConsumerRecord);
        if(flag){
            return mqConsumerRecord.getId();
        }
        return "";
    }
}
