package com.erp.server.dmp.push.consumer;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeStocktakingLossService;
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
 * 盘亏单 同步金蝶
 *
 * @author Lambda
 * @Classname KingdeeStocktakingProfitConsumer
 * @Description TODO
 * @Date 2023-08-14 15:39
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_stocktaking_loss_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_STOCKTAKING_LOSS, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeStocktakingLossConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeStocktakingLossService kingdeeStocktakingLossService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_STOCKCOUNTLOSS.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "PKSH000156"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FKeeperTypeId";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 3);
        System.out.println(queryList);


    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeStocktakingLossService.executeConsumer(map);
        } catch (Exception e) {
            log.error("KingdeeStocktakingLossConsumer>>>onMessage>>>map ={} >>>e={}", map, e);
        }

    }
}
