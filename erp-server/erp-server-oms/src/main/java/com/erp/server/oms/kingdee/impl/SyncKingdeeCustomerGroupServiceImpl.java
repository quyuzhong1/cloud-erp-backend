package com.erp.server.oms.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.dto.base.PushSyncStatusDTO;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.entity.CustomerGroupEntity;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerGroupService;
import com.erp.server.oms.service.CustomerGroupService;
import com.erp.server.oms.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 同步客户到金蝶
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeCustomerGroupServiceImpl implements SyncKingdeeCustomerGroupService {
    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerGroupService customerGroupService;

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void syncDataToKingdee(CustomerGroupEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //更新同步状态为待同步
        PushSyncStatusDTO.KingdeeDTO kingdeeDTO = new PushSyncStatusDTO.KingdeeDTO(entity.getId(),operate,"",SyncStatusEnum.TO_BE_SYNC.getCode());
        customerGroupService.updateSyncKingdeeStatus(kingdeeDTO);

        //金蝶id
        if (StringUtils.isNotBlank(entity.getSyncKingdeeId())) {
            resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        }
        //业务id
        resultMap.put("id", entity.getId());
        //分组名称
        resultMap.put("groupName", entity.getName());

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_CUSTOMER_GROUP_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                PushSyncStatusDTO.KingdeeDTO syncKingdeeDTO = new PushSyncStatusDTO.KingdeeDTO(entity.getId(),operate,"",SyncStatusEnum.IN_SYNC.getCode());
                return customerGroupService.updateSyncKingdeeStatus(syncKingdeeDTO);
            }
            return Boolean.TRUE;
        });
    }
}
