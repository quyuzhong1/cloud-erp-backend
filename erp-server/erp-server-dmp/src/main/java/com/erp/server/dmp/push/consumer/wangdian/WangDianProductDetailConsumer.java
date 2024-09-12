package com.erp.server.dmp.push.consumer.wangdian;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.server.dmp.push.service.wdt.WangDianProductDetailService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsBatchPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC,
        selectorExpression = "wdt_product_detail_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_WDT_PRODUCT_DETAIL,
        consumeMode = ConsumeMode.ORDERLY)
public class WangDianProductDetailConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private WangDianProductDetailService wangDianProductDetailConsumerService;
    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        GoodsBatchPushDTO pushDTOS = JSON.parseObject(ext.toString(), GoodsBatchPushDTO.class);
        wangDianProductDetailConsumerService.executeConsumer(pushDTOS);
        return ApiResult.success();
    }
}
