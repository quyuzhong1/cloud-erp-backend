package com.erp.server.dmp.push.consumer.erp;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.convert.DmpOrderConverter;
import com.erp.server.dmp.service.BiOrderInfoService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private BiOrderInfoService biOrderInfoService;
    @Resource
    private DmpPushTaskService dmpPushTaskService;

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
        String entity = String.valueOf(map.get("entity"));

        SoB2cDTO.ViewDTO viewDTO = JSON.parseObject(entity, SoB2cDTO.ViewDTO.class);
        this.cleanOrderField(viewDTO, operate);
        return ApiResult.success();
    }

    /**
     * 清洗订单
     */
    private void cleanOrderField(SoB2cDTO.ViewDTO viewDTO, String operate) {
        BiOrderInfoEntity biOrderInfoEntity = DmpOrderConverter.INSTANCE.soB2cToDmpOrder(viewDTO);
        String billStatus = viewDTO.getBillStatus();

        //订单状态 1.待配货 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(billStatus)) {
            biOrderInfoEntity.setOrderStatus(1);
        } else if (SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(billStatus)) {
            biOrderInfoEntity.setOrderStatus(2);
        } else if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(billStatus)) {
            biOrderInfoEntity.setOrderStatus(3);
        } else {
            biOrderInfoEntity.setOrderStatus(1);
        }

        List<BiOrderItemSplitEntity> itemEntityList = DmpOrderConverter.INSTANCE.soB2cToDmpOrderItem(viewDTO.getDetailList());
        biOrderInfoEntity.setItemList(itemEntityList);


        //根据操作类型进行操作
        if (Objects.equals(operate, SyncOperateEnum.OPERATE_APPROVE.getCode()) || Objects.equals(operate, SyncOperateEnum.OPERATE_UPDATE.getCode())) {
            //审核
            biOrderInfoService.checkOrder(biOrderInfoEntity);

        } else if (Objects.equals(operate, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()) || Objects.equals(operate, SyncOperateEnum.OPERATE_DELETE.getCode())) {
            //反审核
            biOrderInfoService.removeOrderByCode(Collections.singletonList(biOrderInfoEntity.getPlatformOrderId()));

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
