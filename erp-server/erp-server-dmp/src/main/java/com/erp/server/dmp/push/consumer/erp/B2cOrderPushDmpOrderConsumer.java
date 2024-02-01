package com.erp.server.dmp.push.consumer.erp;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.convert.DmpOrderConverter;
import com.erp.server.dmp.service.DmpOrderInfoService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * ERP的b2c订单推送到金蝶消费者
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_SO_B2C_ORDER_TO_DMP_TOPIC, selectorExpression = "so_b2c_to_dmp_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_ERP_SO_B2C_TO_DMP,
        consumeMode = ConsumeMode.ORDERLY)
public class B2cOrderPushDmpOrderConsumer extends AbstractPlatformConsumerHandler<DmpSyncMqDTO> {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;
    @Resource
    private DmpPushTaskService dmpPushTaskService;

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
        SoB2cDTO.ViewDTO viewDTO = JSONUtil.toBean(ext.toString(), SoB2cDTO.ViewDTO.class);
        this.cleanOrderField(viewDTO);
        return ApiResult.success();
    }

    /**
     * 清洗订单
     */
    private void cleanOrderField(SoB2cDTO.ViewDTO viewDTO) {
        DmpOrderInfoEntity dmpOrderInfoEntity = DmpOrderConverter.INSTANCE.soB2cToDmpOrder(viewDTO);
        String billStatus = viewDTO.getBillStatus();

        //订单状态 1.待配货 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(billStatus)) {
            dmpOrderInfoEntity.setOrderStatus(1);
        } else if (SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(billStatus)) {
            dmpOrderInfoEntity.setOrderStatus(2);
        } else if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(billStatus)) {
            dmpOrderInfoEntity.setOrderStatus(3);
        } else {
            dmpOrderInfoEntity.setOrderStatus(1);
        }

        List<DmpOrderItemEntity> itemEntityList = DmpOrderConverter.INSTANCE.soB2cToDmpOrderItem(viewDTO.getDetailList());
        dmpOrderInfoEntity.setItemList(itemEntityList);
        dmpOrderInfoService.checkOrder(dmpOrderInfoEntity);
    }
}
