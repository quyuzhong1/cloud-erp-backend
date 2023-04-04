package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeCategoryService;
import com.erp.server.plm.service.BasicCategoryService;
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
@Service
public class SyncKingdeeCategoryServiceImpl implements SyncKingdeeCategoryService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private BasicCategoryService basicCategoryService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(BasicCategoryEntity entity) {
        Map<String, Object> resultMap = new HashMap<>();
        //上级id
        String pid = entity.getPid();
        //产品分类
        Integer moduleType = ApiModuleTypeEnum.ONE_LEVEL_CATEGORY.getCode();
        if (!String.valueOf(MathUtil.ZERO).equals(pid)) {
            resultMap.put("moduleType", ApiModuleTypeEnum.TWO_LEVEL_CATEGORY.getCode());
        }
        resultMap.put("moduleType", moduleType);
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("code",entity.getName());

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PRODUCT_DETAIL_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return basicCategoryService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode());
            }
            return Boolean.TRUE;
        });
    }
}
