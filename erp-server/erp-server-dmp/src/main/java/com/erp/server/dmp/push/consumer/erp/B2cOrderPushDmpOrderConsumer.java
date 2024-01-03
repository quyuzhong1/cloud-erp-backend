package com.erp.server.dmp.push.consumer.erp;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.server.dmp.convert.DmpOrderConverter;
import com.erp.server.dmp.service.DmpOrderInfoService;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

import javax.annotation.Resource;

/**
 * ERP的b2c订单推送到金蝶消费者
 */
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_SO_B2C_ORDER_TO_DMP_TOPIC, selectorExpression = "so_b2c_to_dmp_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_ERP_SO_B2C_TO_DMP,
        consumeMode = ConsumeMode.ORDERLY)
public class B2cOrderPushDmpOrderConsumer implements RocketMQListener<DmpSyncMqDTO> {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Override
    public void onMessage(DmpSyncMqDTO dmpSyncMqDTO) {
        SoB2cDTO.ViewDTO viewDTO = JSONObject.parseObject(dmpSyncMqDTO.getMqData(), SoB2cDTO.ViewDTO.class);


    }

    /**
     * 清洗订单
     */
    private void cleanOrderField(SoB2cDTO.ViewDTO viewDTO) {
        DmpOrderInfoEntity dmpOrderInfoEntity = DmpOrderConverter.INSTANCE.soB2cToDmpOrder(viewDTO);

//        dmpOrderInfoService.add()
    }
}
