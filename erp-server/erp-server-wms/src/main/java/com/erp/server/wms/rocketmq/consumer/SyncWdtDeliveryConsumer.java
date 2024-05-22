package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.wdt.WangDianOrderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_wdt_so_out_stock_tag", consumerGroup = RocketMqConsumerGroup.SYNC_WDT_OUT_STOCK_TO_WMS)
public class SyncWdtDeliveryConsumer implements RocketMQListener<String> {
    @Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(String s) {
        //json字符串
        String jsonStr = JSONUtil.toJsonStr(s);
        //json数据
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
        String dmpSyncTaskId = jsonObject.get("dmpSyncTaskId").toString();

        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncTaskId);
        try {
            log.info("监听到旺店通B2C销售出库单要同步：entity>>>>>{}", s);
            WangDianOrderEntity entity = JSONUtil.toBean(jsonStr, WangDianOrderEntity.class);
            syncB2CSoOutstockService.syncWdtSoOutStock(entity);
            //同步成功
            paramDTO.setSyncStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
            paramDTO.setResponseMsg("同步成功");
            dmpTaskFeign.updateSyncInfo(paramDTO);
        } catch (Exception e) {
            log.error("旺店通B2C销售出库单同步失败，msg = {}", StringUtils.isBlank(e.getMessage()) ? e : e.getMessage());
            //同步失败
            paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(StringUtils.isBlank(e.getMessage()) ? ExceptionUtil.stacktraceToOneLineString(e, 10) : e.getMessage());
            dmpTaskFeign.updateSyncInfo(paramDTO);
            //错误预警
            dmpTaskFeign.sendWarnMsg(dmpSyncTaskId);
        }
    }
}
