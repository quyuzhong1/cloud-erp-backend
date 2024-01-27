package com.erp.server.srm.rocketmq.comsumer;

import cn.hutool.json.JSONObject;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.server.srm.service.PurchaseOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class PurchaseOrderConsumer {

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_SCM_TO_SRM_PURCHASE_ORDER_DETAIL_TOPIC,
            selectorExpression = "sync_srm_purchase_order_detail_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-srm_purchase_order_detail_consumer")
    public class ConsumerSrmPurchaseOrder implements RocketMQListener<JSONObject> {
        @Override
        public void onMessage(JSONObject message) {
            //按照订单id维度进行同步
            String id = message.getStr("id");
            List<String> ids = message.getBeanList("ids", String.class);
            List<String> detailIds = message.getBeanList("detailIds", String.class);
            String executionStatus = message.getStr("executionStatus");
            if (executionStatus.equals(ExecutionStatusEnum.CONFIRM.getCode())){
                try {
                    purchaseOrderDetailService.syncScmPurchaseOrderDetail(id, detailIds, executionStatus);
                } catch (Exception e) {
                    log.error("同步SCM采购订单{}数据到SRM异常:{}", id, e.getMessage());
                }
            }else if (executionStatus.equals("invalid")){
                purchaseOrderDetailService.removeBySrmOrderIds(ids);
            }
        }
    }

}

