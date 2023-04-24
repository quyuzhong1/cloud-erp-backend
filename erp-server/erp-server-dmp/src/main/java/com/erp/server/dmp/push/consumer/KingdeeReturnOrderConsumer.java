package com.erp.server.dmp.push.consumer;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 对接金蝶退货单
 * @Author Luo_WG
 * @Date 2023/4/23 19:47
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_return_order_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_RETURN_ORDER)
public class KingdeeReturnOrderConsumer implements RocketMQListener<Map<String, Object>> {
    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PURCHASEORDER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "PO23042400006"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FPOOrderEntry_FEntryID,FMaterialId.FNumber,F_ulz_Combo";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,1);
        System.out.println(queryList);

      /* LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","CGDD-230413-8806");
        JSONObject viewJson = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        System.out.println(viewJson);*/

    }

    @Override
    public void onMessage(Map<String, Object> stringObjectMap) {

    }
}
