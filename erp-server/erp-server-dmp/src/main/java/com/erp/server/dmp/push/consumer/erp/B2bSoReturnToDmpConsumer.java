package com.erp.server.dmp.push.consumer.erp;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.server.dmp.service.BiReturnOrderInfoService;
import com.erp.server.dmp.service.DmpPushTaskService;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 退货入库单同步到中台
 * @param <T>
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_RETURN_ORDER_TO_DMP_TOPIC,
        selectorExpression = "approved_return_order_to_dmp_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_OMS_RETURN_TO_DMP,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class B2bSoReturnToDmpConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private BiReturnOrderInfoService biReturnOrderInfoService;

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

        BiReturnOrderInfoEntity dmpOrderInfoEntity = JSON.parseObject(String.valueOf(map.get("entity")), BiReturnOrderInfoEntity.class);
        this.cleanOrderField(dmpOrderInfoEntity, operate);
        return ApiResult.success();
    }

    /**
     * 清洗订单
     */
    private void cleanOrderField(BiReturnOrderInfoEntity biReturnOrderInfoEntity, String operate) {
        if (ObjectUtil.isEmpty(biReturnOrderInfoEntity)) {
            throw new ServiceException("存储的对象dmpReturnOrderInfoEntity不能为空！");
        }
        //根据操作类型进行操作
        if (Objects.equals(operate, SyncOperateEnum.OPERATE_APPROVE.getCode()) || Objects.equals(operate, SyncOperateEnum.OPERATE_UPDATE.getCode())) {
            //审核
            biReturnOrderInfoService.checkOrder(biReturnOrderInfoEntity);

        } else if (Objects.equals(operate, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()) || Objects.equals(operate, SyncOperateEnum.OPERATE_DELETE.getCode())) {
            //反审核
            biReturnOrderInfoService.removeReturnOrderByCode(Collections.singletonList(String.valueOf(biReturnOrderInfoEntity.getPlatformOrderId())));
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
