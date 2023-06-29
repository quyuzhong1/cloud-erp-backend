package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.DmpTransferInfoEntity;
import com.erp.server.wms.rocketmq.sync.SyncTransferInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_kingdee_transfer_info_to_wms_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_TRANSFER_INFO_TO_WMS)
public class SyncTransferInfoCunsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private SyncTransferInfoService syncTransferInfoService;

    @Override
    public void onMessage(Map<String, Object> map) {
        String dataJson = "";
        log.info("监听到金蝶直接调拨单需要同步：entity={}", dataJson);
        DmpTransferInfoEntity dmpTransferInfoEntity = BeanUtil.toBean(dataJson, DmpTransferInfoEntity.class);
        syncTransferInfoService.syncKingdeeTransferInfo(dmpTransferInfoEntity);
    }

}
