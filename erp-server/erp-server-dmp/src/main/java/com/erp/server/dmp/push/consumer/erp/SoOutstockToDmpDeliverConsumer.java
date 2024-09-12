package com.erp.server.dmp.push.consumer.erp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.BiDeliveryDetailInfoEntity;
import com.erp.server.dmp.service.BiDeliveryDetailInfoService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 销售出库单同步到中台
 * @param <T>
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_SO_OUTSTOCK_ORDER_TO_DMP_TOPIC,
        selectorExpression = "approved_so_outstock_order_to_dmp_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_SO_OUTSTOCK_TO_DMP,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class SoOutstockToDmpDeliverConsumer <T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private BiDeliveryDetailInfoService biDeliveryDetailInfoService;

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
        Map<String, Object> map = JSONUtil.parseObj(ext);

        //操作项
        String operate = String.valueOf(map.get("operate"));

        BiDeliveryDetailInfoEntity biDeliveryDetailInfoEntity = JSON.parseObject(String.valueOf(map.get("entity")), BiDeliveryDetailInfoEntity.class);

        if (CollectionUtil.isEmpty(biDeliveryDetailInfoEntity.getDetails())) {
            return ApiResult.success();
        }
        this.cleanOrderField(biDeliveryDetailInfoEntity, operate);
        return ApiResult.success();
    }

    /**
     * 清洗订单
     */
    private void cleanOrderField(BiDeliveryDetailInfoEntity biDeliveryDetailInfoEntity, String operate) {
        if (ObjectUtil.isEmpty(biDeliveryDetailInfoEntity)) {
            throw new RuntimeException("存储的对象dmpDeliveryDetailInfoEntity不能为空！");
        }
        //根据操作类型进行操作
        if (Objects.equals(operate, SyncOperateEnum.OPERATE_APPROVE.getCode()) || Objects.equals(operate, SyncOperateEnum.OPERATE_UPDATE.getCode())) {
            //审核
            biDeliveryDetailInfoService.checkOrder(biDeliveryDetailInfoEntity);

        } else if (Objects.equals(operate, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()) || Objects.equals(operate, SyncOperateEnum.OPERATE_DELETE.getCode())) {
            //反审核
            biDeliveryDetailInfoService.removeDeliveryByCodes(Collections.singletonList(biDeliveryDetailInfoEntity.getPlatformOrderId()));

        } else if (Objects.equals(operate, SyncOperateEnum.OPERATE_INVALID.getCode())) {
            //作废 不处理
            log.info("作废状态，直接忽略同步dmp订单操作");
//            DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(String.valueOf(code));
//            if (Objects.nonNull(dmpOrderInfoEntity)) {
//                dmpOrderInfoEntity.setOrderStatus(5);
//            }
        }
    }
}
