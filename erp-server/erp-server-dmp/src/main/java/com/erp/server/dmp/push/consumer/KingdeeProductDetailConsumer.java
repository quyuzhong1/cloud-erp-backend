package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.push.service.business.KingdeeProductDetailConsumerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶物料同步
 * @date 2023/3/9 16:24
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_product_detail_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PRODUCT_DETAIL,consumeMode = ConsumeMode.ORDERLY)
public class KingdeeProductDetailConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeProductDetailConsumerService kingdeeProductDetailConsumerService;



    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_MATERIAL.getTaskName());
        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        // viewMap.put("id", "391315");
           viewMap.put("number", "test-sku04");
        //创建组织
        viewMap.put("CreateOrgId", 173616);

        log.info("view方法数据查询,viewJson = {}", JSONUtil.toJsonStr(viewMap));
        JSONObject model = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        JSONObject createOrgId = (JSONObject) model.get("CreateOrgId");
        System.out.println(model);
    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeProductDetailConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeProductDetailConsumer>>>onMessage>>>map ={}", map, e);
        }

    }

}
