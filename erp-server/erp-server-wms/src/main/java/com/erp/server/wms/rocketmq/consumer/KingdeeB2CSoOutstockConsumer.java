package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * B2C 销售出库 金蝶同步到WMS
 *
 * @author Lambda
 * @Classname KingdeeB2CSoOutstockConsumer
 * @Date 2023-06-27 10:21
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_kingdee_so_outatock_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_OUTSTOCK_TO_WMS)
public class KingdeeB2CSoOutstockConsumer implements RocketMQListener<DmpSyncMqDTO> {

    @Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(DmpSyncMqDTO dmpSyncMqDTO) {
        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncMqDTO.getDmpSyncTaskId());
        try {
            String dataJson = dmpSyncMqDTO.getMqData();
            log.info("监听到金蝶B2C销售出库单要同步：entity>>>>>{}", dataJson);
            KingdeeDeliveryDetailEntity entity= JSONObject.parseObject(dataJson,KingdeeDeliveryDetailEntity.class);
            syncB2CSoOutstockService.syncKingdeeSoOutstock(entity);
            //同步成功
            paramDTO.setSyncStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
            paramDTO.setResponseMsg("同步成功");
            dmpTaskFeign.updateSyncInfo(paramDTO);
        }catch (Exception e){
            log.error("金蝶B2C销售出库单同步失败，msg = {}",StringUtils.isBlank(e.getMessage())?e:e.getMessage());
            //同步失败
            paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(StringUtils.isBlank(e.getMessage())? ExceptionUtil.stacktraceToOneLineString(e,10):e.getMessage());
            dmpTaskFeign.updateSyncInfo(paramDTO);
            //错误预警
            dmpTaskFeign.sendWarnMsg(dmpSyncMqDTO.getDmpSyncTaskId());
        }

    }
}
