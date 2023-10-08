package com.erp.server.dmp.push.consumer;

import cn.hutool.core.util.StrUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.server.dmp.push.service.business.KingdeeCustomerConsumerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 对接金蝶仓库
 *
 * @author Lambda
 * @Classname KingdeeWarehouseConsumer
 * @Date 2023-04-25 14:29
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_customer_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_CUSTOMER_INFO, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeCustomerConsumer implements RocketMQListener<Map<String, Object>> {


    @Resource
    private KingdeeCustomerConsumerService kingdeeCustomerConsumerService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils("BD_Customer");
        LinkedList<String> queryFilters = new LinkedList<>();
        // queryFilters.add(String.format("FNumber = '%s'", "CUST23060900001"));
        queryFilters.add(StrUtil.format("FNumber in ({})", "'CUST5188'"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FCUSTID,FForbidStatus";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 11);
        System.out.println(queryList);
        /* JSONObject entries = apiUtils.customerGroupDelete("");*/


      /* LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","CGDD-230413-8806");
        JSONObject viewJson = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        System.out.println(viewJson);*/

    }


    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeCustomerConsumerService.executeCustomerContactConsumer(map);
        }catch (Exception e){
            log.error("KingdeeAssistantDataDetailConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }

    }


}
