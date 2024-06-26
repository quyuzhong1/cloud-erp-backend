package com.erp.server.dmp.push.consumer.wangdian;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.server.dmp.push.service.wdt.WangDianVwPushHandleDetailService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

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
    private WangDianVwPushHandleDetailService wangDianVwPushHandleDetailService;

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
        log.info("虚拟仓订单创建->获取消费数据：{}", ext);
        VwPushHandelDetailPushDTO pushDTOS = JSON.parseObject(ext.toString(), VwPushHandelDetailPushDTO.class);
        //获取当前任务状态-不是成功状态再进行处理
        DmpPushTaskEntity dmpPushTaskEntity = dmpPushTaskService.getById(pushDTOS.getDmpSyncTaskId());
        if (Objects.isNull(dmpPushTaskEntity) || !SyncStatusEnum.SUCCESS_SYNC.getCode().equals(dmpPushTaskEntity.getStatus())) {
            wangDianVwPushHandleDetailService.executeConsumer(pushDTOS);
        }
        return ApiResult.success();
    }
}
