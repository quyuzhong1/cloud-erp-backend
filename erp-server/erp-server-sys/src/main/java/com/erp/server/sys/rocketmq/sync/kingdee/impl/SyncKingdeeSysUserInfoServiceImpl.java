package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.service.SysUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
public class SyncKingdeeSysUserInfoServiceImpl implements SyncKingdeeSysUserInfoService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SysUserInfoService sysUserInfoService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(SysUserInfoEntity entity) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getUid());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("userName",entity.getUserName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //邮箱
        resultMap.put("email", entity.getEmail());
        //电话号码
        resultMap.put("mobile", entity.getMobile());
        //用户状态1：正常 0：禁用
        resultMap.put("userState", entity.getUserState());

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SYS_USER_INFO_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return sysUserInfoService.updateSyncKingdeeStatus(entity.getUid(), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });
    }
}
