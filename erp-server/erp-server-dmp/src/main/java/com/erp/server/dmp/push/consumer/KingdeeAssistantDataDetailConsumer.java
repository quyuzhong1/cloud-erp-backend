package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeAssistantDataDetailConsumerService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶辅助资料同步（产品分类、）
 * @date 2023/3/13 14:45
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_assistant_data_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_ASSISTANT_DATA, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeAssistantDataDetailConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeAssistantDataDetailConsumerService kingdeeAssistantDataDetailConsumerService;


    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "SouthChina"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FEntryId,FNumber,FDataValue,FId,FId.FNumber,FId.FName,FParentId";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 1);

        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        viewMap.put("number", "SouthChina");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(queryList);
        System.out.println(viewJson);

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeAssistantDataDetailConsumerService.executeAssistantDataDetailConsumer(map);
        } catch (Exception e) {
            log.error("KingdeeAssistantDataDetailConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }
    }
}
