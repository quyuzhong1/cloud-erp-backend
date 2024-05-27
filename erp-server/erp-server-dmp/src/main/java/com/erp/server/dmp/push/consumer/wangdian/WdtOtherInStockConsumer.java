package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.server.dmp.push.service.wdt.WdtOtherInStockService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 旺店通其他入库单消费(推送其他入库单到旺店通)
 * @date 2024-05-24
 * @author tanmujin
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC,
        selectorExpression = "wdt_other_in_stock_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_WDT_OTHER_IN_STOCK,
        consumeMode = ConsumeMode.ORDERLY)
public class WdtOtherInStockConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private WdtOtherInStockService wdtPushOtherInStockService;

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
        CreateOtherStockinRequest stockinRequest = JSON.parseObject(JSONUtil.toJsonStr(ext), CreateOtherStockinRequest.class);
        wdtPushOtherInStockService.executeConsumer(stockinRequest);
        return ApiResult.success();
    }
}
