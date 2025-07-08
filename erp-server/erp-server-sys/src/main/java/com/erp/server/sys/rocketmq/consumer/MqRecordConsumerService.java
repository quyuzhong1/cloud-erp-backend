package com.erp.server.sys.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.utils.RedisUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.JsonFieldDiffUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.*;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionExtendTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.server.sys.service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.RECEIVE_DDL_TO_MQ_SYS_TOPIC,
        selectorExpression = "sys_receive_ddl_to_mq_tag",
        consumerGroup = RocketMqConsumerGroup.SYS_RECEIVE_DDL_TO_MQ_CONSUMER)
public class MqRecordConsumerService implements RocketMQListener<String> {

    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @Resource
    private MqConsumerRecordService mqConsumerRecordService;
    @Resource
    private CfgRuleConditionService cfgRuleConditionService;

    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    @Resource
    private RedisUtil redisUtil;

    @Resource
    @Qualifier("thirdNoticePushExecutor")
    private Executor thirdNoticePushExecutor;


    public static final String TABLE_BUSINESS_KEY = "TABLE_BUSINESS_KEY";

    @Override
    public void onMessage(String jsonStr) {
        log.info("MqRecordConsumerService 开始");
        if (StringUtils.isBlank(jsonStr)) {
            return;
        }
        // 创建 Gson 实例
        Gson gson = new Gson();
        List<Map<String, Map<String, Object>>> list = new ArrayList<>();

        //jsonStr 有可能是数组的，也有可能是非数组
        if (jsonStr.startsWith("[")) {//表示数组
            Type mapType = new TypeToken<List<Map<String, Map<String, Object>>>>(){}.getType();
            list = gson.fromJson(jsonStr, mapType);
        }else{
            // 定义嵌套 Map 类型结构： { "before": {...}, "after": {...} }
            Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> jsonMap = gson.fromJson(jsonStr, mapType);
            list.add(jsonMap);
        }

        for (Map<String, Map<String, Object>> jsonMap : list) {
            Map<String, Object> before = jsonMap.getOrDefault("before",null);
            Map<String, Object> after = jsonMap.getOrDefault("after",null);
            if(Objects.isNull(after)){
                continue;
            }

            //只有更新操作才需要进行字段对比
            List<String> diffFields = JsonFieldDiffUtil.compare(before, after);
            if(CollUtil.isEmpty(diffFields)){
                continue;
            }
            //转驼峰
            List<String> convertedDiffFields = diffFields.stream()
                    .map(CharSequenceUtil::toCamelCase)
                    .collect(Collectors.toList());

            thirdNoticePushRecordService.sendThirdNoticeByMqAsync(after,convertedDiffFields);
        }
        log.info("MqRecordConsumerService 结束");
    }
}
