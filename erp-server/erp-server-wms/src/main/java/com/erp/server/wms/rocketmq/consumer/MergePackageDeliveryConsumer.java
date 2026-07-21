package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.WorkflowTaskRecordFeign;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 兼容入口：旧组包自动出库 MQ 仅负责触发 OMS 编排，重逻辑由编排节点执行。
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC,
        selectorExpression = "async_merge_package_delivery_tag",
        consumerGroup = RocketMqConsumerGroup.ASYNC_MERGE_PACKAGE_DELIVERY_CONSUMER,
        consumeThreadNumber = 10
)
public class MergePackageDeliveryConsumer implements RocketMQListener<String> {
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private WorkflowTaskRecordFeign workflowTaskRecordFeign;

    @Override
    public void onMessage(String soId) {
        if (CharSequenceUtil.isBlank(soId)) {
            log.warn("组包自动出库兼容消息为空，跳过触发编排");
            return;
        }
        List<SoB2cDeliveryEntity> deliveryEntities = soB2cDeliveryService.listBySourceIds(Collections.singletonList(soId));
        SoB2cDeliveryEntity curDeliveryEntity = deliveryEntities.stream()
                .filter(v -> !SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(v.getStatus()))
                .findFirst()
                .orElse(null);
        if (curDeliveryEntity == null) {
            log.warn("组包自动出库兼容消息未找到有效发货单，soId={}", soId);
            return;
        }
        try {
            WorkflowTaskRecordDTO.StartWorkflowDTO startDTO = new WorkflowTaskRecordDTO.StartWorkflowDTO();
            startDTO.setSourceId(soId);
            startDTO.setSourceCode(curDeliveryEntity.getSoCode());
            Map<String, Object> firstNodeInputData = new HashMap<>();
            firstNodeInputData.put("soId", soId);
            firstNodeInputData.put("id", soId);
            firstNodeInputData.put("sourceCode", curDeliveryEntity.getSoCode());
            firstNodeInputData.put("triggerSource", "legacyMergePackageConsumer");
            startDTO.setFirstNodeInputData(firstNodeInputData);
            WorkflowTaskRecordDTO.StartWorkflowResultDTO resultDTO = workflowTaskRecordFeign.startMergePackageDeliveryWorkflow(startDTO);
            if (resultDTO == null || !Boolean.TRUE.equals(resultDTO.getAccepted())) {
                throw new RuntimeException(CharSequenceUtil.format("组包自动出库任务受理失败，soId={}", soId));
            }
            log.info("组包自动出库兼容消息已触发编排，soId={}, instanceId={}", soId, resultDTO.getInstanceId());
        } catch (Exception e) {
            log.error("组包自动出库兼容消息触发编排失败，soId={}", soId, e);
            throw new RuntimeException(e);
        }
    }
}
