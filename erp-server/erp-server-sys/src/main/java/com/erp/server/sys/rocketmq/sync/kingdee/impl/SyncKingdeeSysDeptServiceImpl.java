package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysDeptService;
import com.erp.server.sys.service.SysDepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeSysDeptServiceImpl implements SyncKingdeeSysDeptService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SysDepartmentService sysDepartmentService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(String id, String operate) {

        SysDepartmentEntity entity = sysDepartmentService.getById(id);

        if (ObjectUtils.isEmpty(entity)) {
            return;
        }

        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("name",entity.getName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());

        //上级负责部门编码
        SysDepartmentEntity parent = sysDepartmentService.getById(entity.getParentId());
        if (ObjectUtils.isNotEmpty(parent)) {
            resultMap.put("parentCode", parent.getCode());
        }

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SYS_DEPARTMENT_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return sysDepartmentService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });
    }

}
