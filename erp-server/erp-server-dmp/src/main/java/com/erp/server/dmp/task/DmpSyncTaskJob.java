package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.server.dmp.service.DmpSyncTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * DMP推送数据至马其他平台
 * @CreateTime: 2023-06-28  18:44
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class DmpSyncTaskJob {

    @Autowired
    private DmpSyncTaskService dmpSyncTaskService;

    @Autowired
    private MQProducerService mqProducerService;

    /**
     * DMP推送同步任务消息到其他平台
     * @return
     */
    @XxlJob("DmpSyncTaskJob")
    public ReturnT<String> dmpSyncTaskJob() {
        XxlJobHelper.log("DmpSyncTaskJob start");
        // 查询DMP同步数据
        List<DmpSyncTaskEntity> recordEntityList = dmpSyncTaskService.lambdaQuery()
                .in(DmpSyncTaskEntity::getStatus, Arrays.asList(SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .le(DmpSyncTaskEntity::getUpdateTime, LocalDateTime.now().minusHours(1))
                .list();

        // 按修改时间升序
        if(CollUtil.isNotEmpty(recordEntityList)) {
            recordEntityList.sort(Comparator.comparing(DmpSyncTaskEntity::getUpdateTime));
            for (DmpSyncTaskEntity recordEntity : recordEntityList) {
                try {
                   // 发送推送同步任务消息
                    DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(recordEntity.getId(), recordEntity.getMqData());
                    SendResult result = mqProducerService.syncClassMsg(recordEntity.getMqTopic(), recordEntity.getMqTag(),
                            dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
                    if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                        throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                    }
                }catch (Exception e){
                    log.error("从{}推送{}到{}发送消息异常", recordEntity.getSourcePlatformName(), recordEntity.getSouceType(), recordEntity.getTargetPlatformName(), e);
                    XxlJobHelper.log("从{}推送{}到{}发送消息异常", recordEntity.getSourcePlatformName(), recordEntity.getSouceType(), recordEntity.getTargetPlatformName(),  e);
                }
            }
        }
        XxlJobHelper.log("DmpSyncTaskJob end");
        return ReturnT.SUCCESS;
    }

}