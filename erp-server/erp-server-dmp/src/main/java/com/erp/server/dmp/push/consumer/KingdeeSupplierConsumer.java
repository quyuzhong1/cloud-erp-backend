package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSupplierConsumerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Will
 * @version 1.0
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_supplier_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SUPPLIER, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSupplierConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeSupplierConsumerService kingdeeSupplierConsumerService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_SUPPLIER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "GYS23041400015"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FNumber,FFinanceInfo_FEntryID,FPayCondition.FNumber,FContact,FTel,FCommonContactId.FNumber,FsupplierId,FConForbidStatus";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 0);
        System.out.println(queryList);


        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number", "GYS23041400017");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(viewJson);

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeSupplierConsumerService.executeConsumer(map);
        } catch (Exception e) {
            log.error("KingdeeSubcontractOrderConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }

    }


}
