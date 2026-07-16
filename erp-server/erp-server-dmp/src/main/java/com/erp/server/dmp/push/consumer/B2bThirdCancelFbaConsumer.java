package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.rpc.wms.feign.B2bThirdDeliveryFeign;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * B2B 三方发货单取消出库消费。
 * topic/tag 需与 SyncB2bThirdWarehouseServiceImpl.saveCancelTask 写入的 mqTopic/mqTag 一致。
 *
 * @author zdy
 * @version 1.0
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_B2B_THIRD_DELIVERY_CANCEL_ERP_TOPIC,
        selectorExpression = "erp_b2b_third_delivery_cancel_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_B2B_THIRD_DELIVERY_CANCEL,
        consumeMode = ConsumeMode.ORDERLY)
public class B2bThirdCancelFbaConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private B2bThirdDeliveryFeign b2bThirdDeliveryFeign;
    @Resource
    private DmpPushTaskService dmpPushTaskService;

    public static void main(String[] args) {
    }

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
        log.warn("B2BThirdDelivery取消订单请求参数：{}", ext);
        JSONObject jsonObject = JSONUtil.parseObj(ext);
        ThirdWarehouseCancelFbaOutboundReq req = JSONUtil.toBean(jsonObject, ThirdWarehouseCancelFbaOutboundReq.class);
        b2bThirdDeliveryFeign.cancelFbaOutbound(req);
        return ApiResult.success();
    }


}
