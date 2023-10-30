package com.erp.server.dmp.push.consumer.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * DMP接收同步任务推送马帮出入库
 * @CreateTime: 2023-06-28  14:45
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "mabang_inout_stock_tag", consumerGroup = RocketMqConsumerGroup.SYNC_DMP_TRANSFER_INFO_TO_MABANG)
public class DmpMabangInOutStockConsume implements RocketMQListener<DmpSyncMqDTO>  {

    @Autowired
    private DmpPullTaskService dmpPullTaskService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Override
    public void onMessage(DmpSyncMqDTO dtoDmpSyncMqDTO) {
        try {
            MabangInOutStockDTO mabangInOutStockDTO = JSONObject.parseObject(dtoDmpSyncMqDTO.getMqData(), MabangInOutStockDTO.class);
            // erp单号
            String erpSourceCode = mabangInOutStockDTO.getErpSourceCode();
            log.warn("监听到DMP出入库，erp单号【{}】，同步内容：{}", erpSourceCode, JSONObject.toJSONString(dtoDmpSyncMqDTO));

            String syncTaskId = dtoDmpSyncMqDTO.getDmpSyncTaskId();
            DmpPullTaskEntity dmpSyncTaskEntity = dmpPullTaskService.getById(syncTaskId);
            if(Objects.isNull(dmpSyncTaskEntity)) {
                log.warn("未查询到同步到马帮数据，同步任务数据id:{}，待同步内容：{}", syncTaskId, dtoDmpSyncMqDTO.getMqData());
                mabangInOutStockService.sendNoTaskNotice(syncTaskId, erpSourceCode);
                return;
            }
            String sourceType = dmpSyncTaskEntity.getSourceType();
            String sourceTypeName = SourceTypeEnum.getName(sourceType);
            String syncStatusName = SyncStatusEnum.getNameByCode(dmpSyncTaskEntity.getStatus());
            log.warn("ERP【{}】原单据id：【{}】，同步马帮状态【{}】", sourceTypeName, dmpSyncTaskEntity.getSourceId(), syncStatusName);

            // 同步成功的不处理
            if(Objects.equals(dmpSyncTaskEntity.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())) {
                log.warn("ERP【{}】同步到马帮已经同步，不处理", sourceTypeName);
                return;
            }

            // 推送马帮手工出入库
            InventoryInOutEnum inventoryInOutEnum = InventoryInOutEnum.getByCode(mabangInOutStockDTO.getType());
            mabangInOutStockService.sendToMabangInOutStock(dmpSyncTaskEntity, mabangInOutStockDTO, inventoryInOutEnum);
        }catch (Exception e) {
            log.error("DMP接收同步任务推送马帮出入库异常", e);
        }

    }

}