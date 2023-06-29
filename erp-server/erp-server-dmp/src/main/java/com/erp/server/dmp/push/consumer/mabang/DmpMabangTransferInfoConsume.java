package com.erp.server.dmp.push.consumer.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.common.DmpSyncCommonService;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpSyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @CreateTime: 2023-06-28  14:45
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "mabang_inout_stock_tag", consumerGroup = RocketMqConsumerGroup.SYNC_DMP_TRANSFER_INFO_TO_MABANG)
public class DmpMabangTransferInfoConsume implements RocketMQListener<DmpSyncMqDTO>  {

    @Autowired
    private DmpSyncTaskService dmpSyncTaskService;

    @Autowired
    private DmpSyncCommonService dmpSyncCommonService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Override
    public void onMessage(DmpSyncMqDTO dtoDmpSyncMqDTO) {
        log.info("监听到DMP直接调拨单信息->出入库，内容：{}", JSONObject.toJSONString(dtoDmpSyncMqDTO));

        MabangInOutStockDTO mabangInOutStockDTO = JSONObject.parseObject(dtoDmpSyncMqDTO.getMqData(), MabangInOutStockDTO.class);
        // erp直接调拨单单号
        String erpSourceCode = mabangInOutStockDTO.getErpSourceCode();

        //模块类型
        Integer type = ApiModuleTypeEnum.TRANSFER_INFO.getCode();
        PlatformEntity platformEntity = dmpSyncCommonService.getPlatformEntity(erpSourceCode, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        String syncTaskId = dtoDmpSyncMqDTO.getDmpSyncTaskId();
        DmpSyncTaskEntity dmpSyncTaskEntity = dmpSyncTaskService.getById(syncTaskId);
        if(Objects.isNull(dmpSyncTaskEntity)) {
            log.info("未查询到同步数据，同步任务数据id:{}", syncTaskId);
            dmpSyncCommonService.insertLogWriteBackSyncMabangStatus(platformEntity, syncTaskId, "", StrUtil.format("ERP直接调拨单同步到{}未找到同步任务数据", PlatformEnum.MABANG.getDesc()), type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        log.info("ERP直接调拨单id：【{}】，同步马帮状态【{}】", dmpSyncTaskEntity.getSourceId(), dmpSyncTaskEntity.getStatus());

        // 同步成功的不处理
        if(Objects.equals(dmpSyncTaskEntity.getStatus(), "1")) {
            log.info("ERP直接调拨单同步到马帮已经同步，不处理");
            return;
        }
        if(Objects.equals(mabangInOutStockDTO.getType(), "in")) {
            mabangInOutStockService.sendToMabangInStock(dmpSyncTaskEntity, mabangInOutStockDTO );
        } else if(Objects.equals(mabangInOutStockDTO.getType(), "out")) {
            mabangInOutStockService.sendToMabangOutStock(dmpSyncTaskEntity, mabangInOutStockDTO );
        }
    }

}