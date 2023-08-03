package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeCustomerGroupConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

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
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_customer_group_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_CUSTOMER_GROUP, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeCustomerGroupConsumer implements RocketMQListener<Map<String, Object>> {


    @Resource
    private KingdeeCustomerGroupConsumerService kingdeeCustomerGroupConsumerService;

    public static void main(String[] args) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_GROUP.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_CUSTOMER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGTJ23050500001"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FPUR_PATENTRY_FEntryID,FMaterialId.FNumber,FSrcEntryID,FIsPriceListPush";
        map.put("groupName", "B类：100-300万");
        map.put("syncKingdeeId", "1262928");

        JSONObject model = kingdeeCommonService.queryGroupInfo(apiUtils, (String) map.get("syncKingdeeId"), String.valueOf(map.get("groupName")));
        map.put("GroupPkId", String.valueOf(model.get("FID")));

        System.out.println(map.toString());

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeCustomerGroupConsumerService.executeConsumer(map);
        } catch (Exception e) {
            log.error("KingdeeCustomerGroupConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }

    }


}
