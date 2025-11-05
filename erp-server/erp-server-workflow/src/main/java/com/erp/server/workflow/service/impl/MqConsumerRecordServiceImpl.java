package com.erp.server.workflow.service.impl;


import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.erp.model.workflow.entity.MqConsumerRecordEntity;
import com.erp.server.workflow.mapper.MqConsumerRecordMapper;
import com.erp.server.workflow.service.MqConsumerRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.WorkflowMqConsumerRecordDTO;

import java.time.LocalDateTime;
import java.util.Map;

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
public class MqConsumerRecordServiceImpl extends SuperServiceImpl<MqConsumerRecordMapper, MqConsumerRecordEntity> implements MqConsumerRecordService {


    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");


    /**
     * 新增一条mq的消费记录
     */
    @Override
    public String addMqRecord(WorkflowMqConsumerRecordDTO.MqDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        MqConsumerRecordEntity mqConsumerRecord = new MqConsumerRecordEntity();
        mqConsumerRecord.setId(IdWorker.getIdStr());
        mqConsumerRecord.setCreateTime(now);
        mqConsumerRecord.setUpdateTime(now);
        mqConsumerRecord.setCreateUserId("0");
        mqConsumerRecord.setCreateUserName("system");
        mqConsumerRecord.setUpdateUserId("0");
        mqConsumerRecord.setUpdateUserName("system");
        mqConsumerRecord.setTopic(dto.getTopic().replace("${spring.cloud.nacos.discovery.namespace}", namespace));
        mqConsumerRecord.setTag(dto.getTag());
        mqConsumerRecord.setConsumerGroup(dto.getConsumerGroup().replace("${spring.cloud.nacos.discovery.namespace}", namespace));
        Map<String, Object> dataJson = dto.getDataJson();
        JSONUtil.parseObj(dataJson);
        mqConsumerRecord.setDataJson(dataJson);

        int flag = baseMapper.insertMqConsumerRecord(mqConsumerRecord);
//        Boolean flag = save(mqConsumerRecord);
        if(flag >0){
            return mqConsumerRecord.getId();
        }
        return "";
    }
}
