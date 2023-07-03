package com.erp.server.dmp.push.service.mabang.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;

import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpSyncTaskService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


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
    private DmpSyncTaskService dmpSyncTaskService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void inOutStock(MabangInOutStockDTO mabangInOutStock, String sourceId, String sourceCode,
                        String sourceType, String approveType) {
        mabangInOutStock.setApproveType(approveType);

        // 保存任务数据
        DmpSyncTaskEntity dmpSyncTaskEntity = new DmpSyncTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setSouceType(sourceType);
        dmpSyncTaskEntity.setSourceId(sourceId);
        dmpSyncTaskEntity.setSourceCode(sourceCode);
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.MABANG.getDesc());
        dmpSyncTaskEntity.setStatus(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.MABANG_INOUT_STOCK_TAG.getName());
        String mqData = JSONObject.toJSONString(mabangInOutStock);
        dmpSyncTaskEntity.setMqData(mqData);

        dmpSyncTaskService.save(dmpSyncTaskEntity);

        // 发送MQ消息处理发送到马帮
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.MABANG_INOUT_STOCK_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }

    }

    @Override
    public void sendToMabangInStock(DmpSyncTaskEntity dmpSyncTaskEntity, MabangInOutStockDTO mabangInOutStock) {
        // 调用马帮手工入库接口
        Map<String,Object> resultMap = MabangApiUtils.inStorage(PlatformApiEnum.MABANG_IN_STORAGE.getTaskName(), mabangInOutStock);
        boolean isSuccess = (boolean)resultMap.get("success");
        if (isSuccess) {
            JSONObject resultJson = (JSONObject)resultMap.get("result");
            // 新增出入库记录

            // 更新出入库同步信息
            dmpSyncTaskService.updateSyncInfo(dmpSyncTaskEntity.getId(), SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode(), resultJson.toJSONString());
        } else {
            String msg = StrUtils.null2EmptyWithTrim(resultMap.get("msg"));
            // 更新出入库同步信息
            dmpSyncTaskService.updateSyncInfo(dmpSyncTaskEntity.getId(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode(), msg);

            String sourceType = dmpSyncTaskEntity.getSouceType();
            String sourceTypeName = StrUtils.null2EmptyWithTrim(SourceTypeEnum.getName(sourceType));

            WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
            warnMsgInfoDTO.setTitle(StrUtil.format("ERP{}推送马帮手工入库异常", sourceTypeName));
            warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
            warnMsgInfoDTO.setBizName(StrUtil.format("ERP{}推送马帮手工入库", sourceTypeName));
            warnMsgInfoDTO.setTableName("dmp_sync_task");
            warnMsgInfoDTO.setTableId(dmpSyncTaskEntity.getId());
            warnMsgInfoDTO.setKeyInfo(StrUtil.format("ERP{}单据编号: {}",sourceTypeName, mabangInOutStock.getErpSourceCode()));
            mqProducerService.sendWarnMsg(warnMsgInfoDTO);
        }
    }

    @Override
    public void sendToMabangOutStock(DmpSyncTaskEntity dmpSyncTaskEntity, MabangInOutStockDTO mabangInOutStock) {
        // 调用马帮手工出库接口
        Map<String,Object> resultMap = MabangApiUtils.outStorage(PlatformApiEnum.MABANG_OUT_STORAGE.getTaskName(), mabangInOutStock);
        boolean isSuccess = (boolean)resultMap.get("success");
        if(isSuccess) {
            JSONObject resultJson = (JSONObject)resultMap.get("result");
            // 更新出入库同步信息
            dmpSyncTaskService.updateSyncInfo(dmpSyncTaskEntity.getId(), SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode(), resultJson.toJSONString());
        } else {
            String msg = StrUtils.null2EmptyWithTrim(resultMap.get("msg"));
            // 更新出入库同步信息
            dmpSyncTaskService.updateSyncInfo(dmpSyncTaskEntity.getId(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode(), msg);

            String sourceType = dmpSyncTaskEntity.getSouceType();
            String sourceTypeName = StrUtils.null2EmptyWithTrim(SourceTypeEnum.getName(sourceType));

            WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
            warnMsgInfoDTO.setTitle(StrUtil.format("ERP{}推送马帮手工出库异常", sourceTypeName));
            warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
            warnMsgInfoDTO.setBizName(StrUtil.format("ERP{}推送马帮手工出库", sourceTypeName));
            warnMsgInfoDTO.setTableName("dmp_sync_task");
            warnMsgInfoDTO.setTableId(dmpSyncTaskEntity.getId());
            warnMsgInfoDTO.setKeyInfo(StrUtil.format("ERP{}单据编号: {}",sourceTypeName, mabangInOutStock.getErpSourceCode()));
            mqProducerService.sendWarnMsg(warnMsgInfoDTO);
        }
    }

}