package com.erp.server.dmp.push.consumer.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpSyncTaskService;
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
    private DmpSyncTaskService dmpSyncTaskService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Autowired
    private MQProducerService mqProducerService;

    @Override
    public void onMessage(DmpSyncMqDTO dtoDmpSyncMqDTO) {
        MabangInOutStockDTO mabangInOutStockDTO = JSONObject.parseObject(dtoDmpSyncMqDTO.getMqData(), MabangInOutStockDTO.class);
        // erp单号
        String erpSourceCode = mabangInOutStockDTO.getErpSourceCode();
        log.warn("监听到DMP出入库，erp单号【{}】，同步内容：{}", erpSourceCode, JSONObject.toJSONString(dtoDmpSyncMqDTO));

        String syncTaskId = dtoDmpSyncMqDTO.getDmpSyncTaskId();
        DmpSyncTaskEntity dmpSyncTaskEntity = dmpSyncTaskService.getById(syncTaskId);
        if(Objects.isNull(dmpSyncTaskEntity)) {
            log.warn("未查询到同步到马帮数据，同步任务数据id:{}，待同步内容：{}", syncTaskId, dtoDmpSyncMqDTO.getMqData());
            this.sendNotice(syncTaskId, erpSourceCode);
            return;
        }
        String sourceType = dmpSyncTaskEntity.getSourceType();
        String sourceTypeName = SourceTypeEnum.getName(sourceType);
        String syncStatusName = SyncKingdeeStatusEnum.getNameByCode(dmpSyncTaskEntity.getStatus());
        log.warn("ERP【{}】原单据id：【{}】，同步马帮状态【{}】", sourceTypeName, dmpSyncTaskEntity.getSourceId(), syncStatusName);

        // 同步成功的不处理
        if(Objects.equals(dmpSyncTaskEntity.getStatus(), SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode())) {
            log.warn("ERP【{}】同步到马帮已经同步，不处理", sourceTypeName);
            return;
        }

        // 推送马帮手工出入库
        InventoryInOutEnum inventoryInOutEnum = InventoryInOutEnum.getByCode(mabangInOutStockDTO.getType());
        mabangInOutStockService.sendToMabangInOutStock(dmpSyncTaskEntity, mabangInOutStockDTO, inventoryInOutEnum);
    }

    /**
     * 未找到同步任务消息通知
     * @param syncTaskId
     * @param erpSourceCode
     */
    private void sendNotice(String syncTaskId, String erpSourceCode){
        String errMsg = StrUtil.format("未找到同步任务id：【{}】，ERP单据编号: {}", syncTaskId, erpSourceCode);
        log.info(errMsg);
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle("ERP推送马帮手工出库异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName("ERP推送马帮手工出库");
        warnMsgInfoDTO.setTableName("dmp_sync_task");
        warnMsgInfoDTO.setTableId(syncTaskId);
        warnMsgInfoDTO.setKeyInfo(errMsg);
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }

}