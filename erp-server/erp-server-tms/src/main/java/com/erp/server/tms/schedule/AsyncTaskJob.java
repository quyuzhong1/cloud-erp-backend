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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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

    /**
     *
     *异步任务状态更新
     * @return
     */
    @XxlJob("TmsAsyncTaskJob")
    public ReturnT<String> TmsAsyncTaskJob() {
        asyncTaskRecordService.startTask();
        return ReturnT.SUCCESS;
    }
}
