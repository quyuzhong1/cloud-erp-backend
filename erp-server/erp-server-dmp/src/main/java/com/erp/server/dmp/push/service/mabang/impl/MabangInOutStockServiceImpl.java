package com.erp.server.dmp.push.service.mabang.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;

import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.utils.MabangApiUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 马帮手工出入库实现类
 * @CreateTime: 2023-06-27  19:51
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class MabangInOutStockServiceImpl implements MabangInOutStockService {

    @Autowired
    private MQProducerService mqProducerService;

    @Autowired
    private DmpPullTaskService dmpPullTaskService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void inOutStock(MabangInOutStockDTO mabangInOutStock, String sourceId, String sourceCode,
                        String sourceType, String approveType) {
        // 保存任务数据
        DmpPullTaskEntity dmpSyncTaskEntity = new DmpPullTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setSourceType(sourceType);
        dmpSyncTaskEntity.setSourceId(sourceId);
        dmpSyncTaskEntity.setSourceCode(sourceCode);
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.MABANG.getDesc());
        dmpSyncTaskEntity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.MABANG_INOUT_STOCK_TAG.getName());
        String mqData = JSONObject.toJSONString(mabangInOutStock);
        dmpSyncTaskEntity.setMqData(mqData);

        dmpPullTaskService.save(dmpSyncTaskEntity);

        // 此处防止数据库还未保存成功，MQ先消费
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("保存同步任务成功，待同步的内容为：{}", JSONObject.toJSONString(dmpSyncTaskEntity));
                // 发送MQ消息处理发送到马帮
                DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
                SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.MABANG_INOUT_STOCK_TAG.getName(),
                        dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
                if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                    throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                }
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchInOutStock(List<MabangInOutStockDTO> mabangInOutStockDTOList, String sourceId, String sourceCode, String sourceType, String approveType) {
        if(CollUtil.isEmpty(mabangInOutStockDTOList)) {
            log.warn("没有出入库数据，来源单据类型：【{}】,来源单据id：【{}】,来源单据编码：【{}】，操作类型：【{}】", sourceType, sourceId, sourceCode, approveType);
            return;
        }

        List<DmpPullTaskEntity> dmpSyncTaskList = Lists.newArrayList();
        for(MabangInOutStockDTO mabangInOutStock : mabangInOutStockDTOList) {
            // 保存任务数据
            DmpPullTaskEntity dmpSyncTaskEntity = new DmpPullTaskEntity();
            dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskEntity.setSourceType(sourceType);
            dmpSyncTaskEntity.setSourceId(sourceId);
            dmpSyncTaskEntity.setSourceCode(sourceCode);
            dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.MABANG.getDesc());
            dmpSyncTaskEntity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
            dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.MABANG_INOUT_STOCK_TAG.getName());
            String mqData = JSONObject.toJSONString(mabangInOutStock);
            dmpSyncTaskEntity.setMqData(mqData);

            dmpPullTaskService.save(dmpSyncTaskEntity);
            dmpSyncTaskList.add(dmpSyncTaskEntity);
        }

        // 此处防止数据库还未保存成功，MQ先消费
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for(DmpPullTaskEntity dmpSyncTaskEntity : dmpSyncTaskList) {
                    log.warn("保存同步任务成功，目标平台：{}, 待同步的单据类型：{}，单据编号：{}", dmpSyncTaskEntity.getTargetPlatformName(), dmpSyncTaskEntity.getSourceType(), dmpSyncTaskEntity.getSourceCode());

                    // 发送MQ消息处理发送到马帮
                    DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), dmpSyncTaskEntity.getMqData());
                    SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.MABANG_INOUT_STOCK_TAG.getName(),
                            dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
                    if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                        throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                    }

                }
            }
        });

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendToMabangInOutStock(DmpPullTaskEntity dmpSyncTaskEntity, MabangInOutStockDTO mabangInOutStock, InventoryInOutEnum inventoryInOutEnum) {
        String taskName = "";
        if(Objects.equals(inventoryInOutEnum, InventoryInOutEnum.IN_STOCK)) {
            taskName = PlatformApiEnum.MABANG_IN_STORAGE.getTaskName();
        } else if(Objects.equals(inventoryInOutEnum, InventoryInOutEnum.OUT_STOCK)) {
            taskName = PlatformApiEnum.MABANG_OUT_STORAGE.getTaskName();
        }
        // 调用马帮手工出入库接口
        Map<String,Object> resultMap = MabangApiUtils.inOutStorage(taskName, mabangInOutStock);
        boolean isSuccess = (boolean)resultMap.get("success");
        if(isSuccess) {
            JSONObject resultJson = (JSONObject)resultMap.get("result");
            // 更新出入库同步信息
            dmpPullTaskService.updateSyncInfo(dmpSyncTaskEntity.getId(), SyncStatusEnum.SUCCESS_SYNC.getCode(), resultJson.toJSONString());
        } else {
            String msg = StrUtils.null2EmptyWithTrim(resultMap.get("msg"));
            // 更新出入库同步信息
            dmpPullTaskService.updateSyncInfo(dmpSyncTaskEntity.getId(), SyncStatusEnum.FAILED_SYNC.getCode(), msg);

            String sourceType = dmpSyncTaskEntity.getSourceType();
            String sourceTypeName = StrUtils.null2EmptyWithTrim(SourceTypeEnum.getName(sourceType));

            WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
            warnMsgInfoDTO.setTitle(StrUtil.format("ERP{}推送马帮手工{}异常", sourceTypeName, inventoryInOutEnum.getName()));
            warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
            warnMsgInfoDTO.setBizName(StrUtil.format("ERP{}推送马帮手工{}", sourceTypeName, inventoryInOutEnum.getName()));
            warnMsgInfoDTO.setTableName("dmp_sync_task");
            warnMsgInfoDTO.setTableId(dmpSyncTaskEntity.getId());
            warnMsgInfoDTO.setKeyInfo(StrUtil.format("ERP{}单据编号: {}，失败原因：{}",sourceTypeName, mabangInOutStock.getErpSourceCode(), msg));
            mqProducerService.sendWarnMsg(warnMsgInfoDTO);
        }
    }

    @Override
    public void sendNoticeNoMonitorWarehouse(String sourceTypeName) {
        log.warn(StrUtil.format("ERP{}同步到马帮出入库未配置监控仓库", sourceTypeName));
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle(StrUtil.format("ERP{}推送马帮出入库未配置监控仓库", sourceTypeName));
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName(StrUtil.format("ERP{}推送马帮手工入库", sourceTypeName));
        warnMsgInfoDTO.setTableName("");
        warnMsgInfoDTO.setTableId("");
        warnMsgInfoDTO.setKeyInfo(StrUtil.format("ERP{}同步到马帮出入库未配置监控仓库，请在cfg_setting表erp_to_mb_direct_transfer配置", sourceTypeName));
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }

    @Override
    public void sendNoTaskNotice(String syncTaskId, String erpSourceCode) {
        String errMsg = StrUtil.format("未找到同步任务id：【{}】，ERP单据编号: {}", syncTaskId, erpSourceCode);
        log.info(errMsg);
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle("ERP推送马帮手工出入库异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName("ERP推送马帮手工出入库");
        warnMsgInfoDTO.setTableName("dmp_sync_task");
        warnMsgInfoDTO.setTableId(syncTaskId);
        warnMsgInfoDTO.setKeyInfo(errMsg);
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }

}