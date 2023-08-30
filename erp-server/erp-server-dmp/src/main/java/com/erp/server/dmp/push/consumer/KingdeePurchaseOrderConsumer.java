package com.erp.server.dmp.push.consumer;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeePurchaseOrderConsumerService;
import com.sdk.third.kingdee.utils.KingdeeApiUtils;
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
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_order_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_ORDER, consumeMode = ConsumeMode.ORDERLY)
public class KingdeePurchaseOrderConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeePurchaseOrderConsumerService kingdeePurchaseOrderConsumerService;



    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PURCHASEORDER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGDD-230706-10772"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FModifyDate,FPurchaserId.FNumber,FPOOrderEntry_FEntryID,FMaterialId.FNumber";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 20);
        queryList.forEach(req -> {
            System.out.println(req);
        });



    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeePurchaseOrderConsumerService.executeConsumer(map);
        } catch (Exception e) {
            log.error("KingdeePurchaseOrderConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }


    }


}
