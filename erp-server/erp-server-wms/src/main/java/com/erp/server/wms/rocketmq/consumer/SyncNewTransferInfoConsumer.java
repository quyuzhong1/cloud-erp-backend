package com.erp.server.wms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncTransferInfoService;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_GROUP)
public class SyncNewTransferInfoConsumer implements RocketMQListener<Object> {

	@Resource
    private SyncTransferInfoService syncTransferInfoService;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Override
    public void onMessage(Object ext) {
        //json数据
    	JSONObject jsonObject = JSON.parseObject(ext.toString());
        String dmpOutputTaskRecordId = jsonObject.get("dmpOutputTaskRecordId").toString();

        DmpOutputTaskRecordDTO.UpdateDTO updateDTO = new DmpOutputTaskRecordDTO.UpdateDTO();
        updateDTO.setId(dmpOutputTaskRecordId);
        updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.FINISH.getCode());
        log.info("监听到金蝶直接调拨单需要同步：entity={}", jsonObject);
        DmpTransferInfoDTO dmpTransferInfoDTO = JSON.parseObject(ext.toString(),  DmpTransferInfoDTO.class);
        try {
            syncTransferInfoService.syncKingdeeTransferInfo(dmpTransferInfoDTO);
        } catch (Throwable e) {
            log.error("金蝶直接调拨单同步失败，msg = {}",e.getMessage());
            updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode());
            updateDTO.setResponseData("消费数据失败：" + ExceptionUtil.stacktraceToOneLineString(e));
        }
        dmpInoutTaskFeign.updateOutputTaskRecord(updateDTO);
    }

}

