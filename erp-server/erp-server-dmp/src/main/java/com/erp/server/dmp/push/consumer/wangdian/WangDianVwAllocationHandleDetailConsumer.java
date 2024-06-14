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
import com.erp.server.dmp.push.service.wdt.WangDianVwAllocationHandleDetailService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsBatchPushDTO;
import com.sdk.wangdian.sdk.api.goods.dto.VwAllocationHandelDetailPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC,
        selectorExpression = "wdt_virtual_allocation_handle_detail_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL,
        consumeMode = ConsumeMode.ORDERLY)
public class WangDianVwAllocationHandleDetailConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private WangDianVwAllocationHandleDetailService wangDianVwAllocationHandleDetailService;
    @Override
    public void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg) {
        dmpPushTaskService.updateStatus(new DmpSyncMqDTO.ParamDTO(syncTaskId, code.getCode(), msg));

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
        VwAllocationHandelDetailPushDTO pushDTOS = JSON.parseObject(ext.toString(), VwAllocationHandelDetailPushDTO.class);
        wangDianVwAllocationHandleDetailService.executeConsumer(pushDTOS);
        return ApiResult.success();
    }
}
