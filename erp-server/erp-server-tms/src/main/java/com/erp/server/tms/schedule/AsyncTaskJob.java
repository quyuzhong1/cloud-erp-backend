package com.erp.server.tms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordExecTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @ClassName AsyncTaskJob
 * @description: 异步任务状态更新
 * @date 2026-01-30
 */
@Component
@Slf4j
@EnableScheduling
public class AsyncTaskJob {

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;
    @Resource
    private MQProducerService mQProducerService;

    /**
     *
     *异步任务状态更新
     * @return
     */
    @XxlJob("TmsAsyncTaskJob")
    public ReturnT<String> TmsAsyncTaskJob() {
        //查询所有
        List<TmsAsyncTaskRecordEntity> list = asyncTaskRecordService.lambdaQuery()
                .in(TmsAsyncTaskRecordEntity::getStatus, Arrays.asList(TmsAsyncTaskRecordStatusEnum.ING.getCode(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
//                .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
//                .eq(TmsAsyncTaskRecordEntity::getExecType, TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode())
                .list();
        if(CollUtil.isEmpty(list)){
            log.error("TmsAsyncTaskRecord不存在待执行或者执行中的任务");
            return ReturnT.SUCCESS;
        }

        //执行中
        LocalDateTime now = LocalDateTime.now();
        List<TmsAsyncTaskRecordEntity> ingList = list.stream().filter(e -> e.getStatus().equals(TmsAsyncTaskRecordStatusEnum.ING.getCode())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(ingList)){
            for (TmsAsyncTaskRecordEntity entity : ingList) {
                Integer execTimeout = entity.getExecTimeout();
                LocalDateTime startTime = entity.getStartTime();
                if(Objects.nonNull(startTime) && Objects.nonNull(execTimeout) && execTimeout > 0 ){
                    if(now.isAfter(startTime.plusSeconds(execTimeout))){
                        asyncTaskRecordService.terminateTaskTimeout(entity.getId(),"任务执行超时");
                    }
                }
            }
        }

        //待执行
        List<TmsAsyncTaskRecordEntity> pendingList = list.stream()
                .filter(e -> e.getExecType().equals(TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode()) && e.getStatus().equals(TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
                .collect(Collectors.toList());
        if(CollUtil.isNotEmpty(pendingList)){
            for (TmsAsyncTaskRecordEntity entity : pendingList) {
                String dataJson = entity.getDataJson();
                if(StringUtils.isBlank(dataJson) || Objects.equals(dataJson,"{}")){
                    asyncTaskRecordService.updateTask(entity.getId(),TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),"dataJson为空直接结束任务");
                    continue;
                }

                TmsAsyncTaskRecordDTO.PushParamsDTO taskDTO = JSONUtil.toBean(entity.getDataJson(), TmsAsyncTaskRecordDTO.PushParamsDTO.class);
                taskDTO.setTaskId(entity.getId());
                SendResult sendResult = mQProducerService.syncClassMsg(RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC, RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG, taskDTO, taskDTO.getTaskId());
                if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                    log.error("消息发送结果失败：{}", JSONObject.toJSONString(sendResult));
                }else {
                    log.error("MQ数据结果：{}", JSONUtil.toJsonStr(sendResult));
                }
            }
        }
        return ReturnT.SUCCESS;
    }
}
