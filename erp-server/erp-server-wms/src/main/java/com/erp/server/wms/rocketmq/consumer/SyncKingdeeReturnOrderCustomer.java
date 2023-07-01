package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_kingdee_return_order_to_wms_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_RETURN_ORDER_TO_WMS)
public class SyncKingdeeReturnOrderCustomer implements RocketMQListener<DmpSyncMqDTO> {

    @Resource
    private SyncSoReturnService syncSoReturnService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(DmpSyncMqDTO dmpSyncMqDTO) {
        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncMqDTO.getDmpSyncTaskId());
        try {
            String dataJson = dmpSyncMqDTO.getMqData();
            log.info("监听到金蝶销售退货单要同步：entity>>>>>{}", dataJson);
            KingdeeReturnOrderEntity entity= BeanUtil.toBean(JSONUtil.parseObj(dataJson), KingdeeReturnOrderEntity.class);
            syncSoReturnService.syncKingdeeReturnOrderToSoReturn(entity);
        }catch (Exception e){
            log.error("金蝶销售退货单同步失败，msg = {}",e.getMessage());
            //同步失败
            paramDTO.setSyncStatus(SyncKingdeeStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(e.getMessage());
            dmpTaskFeign.updateSyncInfo(paramDTO);
        }
        //同步成功
        paramDTO.setSyncStatus(SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode());
        paramDTO.setResponseMsg("同步成功");
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }
}
