package com.erp.server.dmp.push.consumer;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeReturnOrderConsumerService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 对接金蝶退货单
 *
 * @Author Luo_WG
 * @Date 2023/4/23 19:47
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_return_order_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_RETURN_ORDER, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeReturnOrderConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeReturnOrderConsumerService kingdeeReturnOrderConsumerService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_MRB.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGTL2017741"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FMRMODE,FMRTYPE,FBillNo";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 1);
        System.out.println(queryList);

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeReturnOrderConsumerService.executeConsumer(map);
        } catch (Exception e) {
            log.error("KingdeeReturnOrderConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }
    }
}
