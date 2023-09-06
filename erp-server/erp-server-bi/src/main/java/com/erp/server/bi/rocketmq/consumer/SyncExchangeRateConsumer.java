package com.erp.server.bi.rocketmq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.DmpExchangeRateDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.bi.rocketmq.sync.SyncExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_kingdee_exchange_rate_to_wms_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_EXCHANGE_RATE_TO_WMS)
public class SyncExchangeRateConsumer implements RocketMQListener<DmpSyncMqDTO> {

    @Resource
    private SyncExchangeRateService syncExchangeRateService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(DmpSyncMqDTO dmpSyncMqDTO) {
        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncMqDTO.getDmpSyncTaskId());
        String dataJson = dmpSyncMqDTO.getMqData();
        log.info("监听到汇率列表需要同步：entity={}", dataJson);
        DmpExchangeRateDTO dmpExchangeRateDTO = BeanUtil.toBean(JSONUtil.parseObj(dataJson), DmpExchangeRateDTO.class);
        try {
            syncExchangeRateService.syncKingdeeExchangeRate(dmpExchangeRateDTO);
        } catch (Exception e) {
            log.error("金蝶汇率列表同步失败，msg = {}",e.getMessage());
            //同步失败
            paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(e.getMessage());
            dmpTaskFeign.updateSyncInfo(paramDTO);
            return;
        }
        //同步成功
        paramDTO.setSyncStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        paramDTO.setResponseMsg("同步成功");
        dmpTaskFeign.updateSyncInfo(paramDTO);

    }

}

