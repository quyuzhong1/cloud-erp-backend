package com.erp.server.dmp.push.consumer.erp;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.dmp.convert.DmpOrderConverter;
import com.erp.server.dmp.service.DmpDeliveryDetailInfoService;
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
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_SO_B2C_DELIVERY_TO_DMP_TOPIC, selectorExpression = "so_b2c_delivery_to_dmp_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_ERP_SO_B2C_DELIVERY_TO_DMP,
        consumeMode = ConsumeMode.ORDERLY)
public class B2cDeliveryPushDmpDeliveryConsumer extends AbstractPlatformConsumerHandler<DmpSyncMqDTO> {
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;
    @Resource
    private SoB2cFeign soB2cFeign;

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
        DmpSyncMqDTO dto = JSONUtil.toBean(JSONObject.toJSONString(ext), DmpSyncMqDTO.class);
        SoB2cDeliveryDTO.ViewDTO viewDTO = JSONObject.parseObject(dto.getMqData(), SoB2cDeliveryDTO.ViewDTO.class);
        this.cleanOrderField(viewDTO);
        return ApiResult.success();
    }

    /**
     * 清洗订单
     */
    private void cleanOrderField(SoB2cDeliveryDTO.ViewDTO viewDTO) {

        SoB2cDTO.ViewDTO soB2cView = soB2cFeign.view(viewDTO.getSourceId());
        if (ObjectUtil.isEmpty(soB2cView)) {
            log.info("同步B2C发货单到DMP时未找到上游的订单=====》" + viewDTO.getSourceCode());
            return;
        }

        DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity = DmpOrderConverter.INSTANCE.soB2cDeliveryToDmpDelivery(viewDTO, soB2cView);
        List<DmpDeliveryDetailItemEntity> itemEntityList = DmpOrderConverter.INSTANCE.soB2cDeliveryDetailToDmpDeliveryItem(viewDTO.getDetailList());
        dmpDeliveryDetailInfoEntity.setDetails(itemEntityList);

        dmpDeliveryDetailInfoService.checkOrder(dmpDeliveryDetailInfoEntity);
    }
}
