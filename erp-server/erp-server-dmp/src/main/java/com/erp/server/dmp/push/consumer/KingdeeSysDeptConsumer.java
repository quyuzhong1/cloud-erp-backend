package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSysDeptConsumerService;
import com.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Will
 * @version 1.0
 * @date 2023/4/10 11:56
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_sys_department_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SYS_DEPARTMENT, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSysDeptConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeSysDeptConsumerService kingdeeSysDeptConsumerService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_DEPARTMENT.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "23041200001"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FNumber,FForbidDate";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 1);

        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number", "23041200001");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(queryList);
        //System.out.println(viewJson);

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeSysDeptConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeSysDeptConsumer>>>onMessage>>>map ={}>>>e={}", map, e);

        }
    }
}


