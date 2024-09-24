package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.server.dmp.service.DmpPullTaskHistoryService;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.service.DmpPushTaskHistoryService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.server.dmp.service.impl.DmpPushTaskHistoryServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private DmpPullTaskService dmpPullTaskService;

    @Autowired
    private DmpPushTaskService dmpPushTaskService;

    @Autowired
    private MQProducerService mqProducerService;

    @Resource
    private DmpPullTaskHistoryService dmpPullTaskHistoryService;
    @Resource
    private DmpPushTaskHistoryService dmpPushTaskHistoryService;

    /**
     * DMP推送同步任务消息到其他平台
     * @return
     */
    @XxlJob("DmpSyncTaskJob")
    public ReturnT<String> dmpPullTaskJob() {
        XxlJobHelper.log("DmpSyncTaskJob start");
        String jobParam = XxlJobHelper.getJobParam();
        Integer diffMinute = 60;
        Integer size = 1000;
        if(StrUtil.isNotBlank(jobParam)){
            XxlJobHelper.log("DmpSyncTaskJob jobParam:{}", jobParam);
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            diffMinute = jsonParam.getInt("diffMinute", 60);
            size = jsonParam.getInt("size", 1000);
        }

        // 查询DMP同步数据
        List<DmpPullTaskEntity> recordEntityList = dmpPullTaskService.lambdaQuery()
                .in(DmpPullTaskEntity::getStatus, Arrays.asList(SyncStatusEnum.FAILED_SYNC.getCode(), SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.IN_SYNC.getCode()))
                .le(DmpPullTaskEntity::getUpdateTime, LocalDateTime.now().minusMinutes(diffMinute))
                .orderByAsc(DmpPullTaskEntity::getUpdateTime)
                .last(null != size && size > 0, StrUtil.format("limit {}", size))
                .list();
        XxlJobHelper.log("DmpSyncTaskJob 查询到{}条待推送数据", recordEntityList.size());
        // 按修改时间升序
        if(CollectionUtil.isEmpty(recordEntityList)) {
            return ReturnT.SUCCESS;
        }
        recordEntityList.sort(Comparator.comparing(DmpPullTaskEntity::getUpdateTime));
        for (DmpPullTaskEntity recordEntity : recordEntityList) {
            try {
                // 发送推送同步任务消息
                JSONObject jsonObject = JSONUtil.parseObj(recordEntity.getMqData());
                jsonObject.set("dmpSyncTaskId",recordEntity.getId());
                SendResult result = mqProducerService.syncClassMsg(recordEntity.getMqTopic(), recordEntity.getMqTag(),
                        jsonObject, recordEntity.getSourceId());
                if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                    throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                }
            }catch (Exception e){
                String sourceTypeName = SourceTypeEnum.getName(recordEntity.getSourceType());
                log.error("从{}推送{}到{}发送消息异常", recordEntity.getSourcePlatformName(), sourceTypeName, recordEntity.getTargetPlatformName(), e);
                XxlJobHelper.log("从{}推送{}到{}发送消息异常", recordEntity.getSourcePlatformName(), recordEntity.getSourceType(), recordEntity.getTargetPlatformName(),  e);
            }
        }
        XxlJobHelper.log("DmpSyncTaskJob end");
        return ReturnT.SUCCESS;
    }

    /**
     * DMP推送同步任务消息到其他平台
     * @return
     */
    @XxlJob("DmpPushTaskJob")
    public ReturnT<String> dmpPushTaskJob() {
        XxlJobHelper.log("DmpPushTaskJob start");
        String jobParam = XxlJobHelper.getJobParam();
        Integer diffMinute = 60;
        Integer size = 1000;
        if(StrUtil.isNotBlank(jobParam)){
            XxlJobHelper.log("DmpPushTaskJob jobParam:{}", jobParam);
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            diffMinute = jsonParam.getInt("diffMinute", 60);
            size = jsonParam.getInt("size", 1000);
        }

        // 查询DMP同步数据
        List<DmpPushTaskEntity> recordEntityList = dmpPushTaskService.lambdaQuery()
                .in(DmpPushTaskEntity::getStatus, Arrays.asList(SyncStatusEnum.FAILED_SYNC.getCode(), SyncStatusEnum.TO_BE_SYNC.getCode(),SyncStatusEnum.IN_SYNC.getCode()))
                .le(DmpPushTaskEntity::getUpdateTime, LocalDateTime.now().minusMinutes(diffMinute))
                .orderByAsc(DmpPushTaskEntity::getUpdateTime)
                .last(null != size && size > 0, StrUtil.format("limit {}", size))
                .list();
        XxlJobHelper.log("DmpPushTaskJob 查询到{}条待推送数据", recordEntityList.size());
        // 按修改时间升序
        if(CollectionUtil.isEmpty(recordEntityList)) {
            return ReturnT.SUCCESS;
        }
        recordEntityList.sort(Comparator.comparing(DmpPushTaskEntity::getUpdateTime));
        //需要修改备注信息
        List<DmpPushTaskEntity> updateList = new ArrayList<>();
        for (DmpPushTaskEntity recordEntity : recordEntityList) {
            try {
                //查询来源上级单据
                Boolean isSend = dmpPushTaskService.isSendParentBillTask(recordEntity);
                //判断是否存在上级单据，并且推送成功
                if (!isSend) {
                    updateList.add(recordEntity);
                    continue;
                }
                if (SyncStatusEnum.TO_BE_SYNC.getCode().equals(recordEntity.getStatus())) {
                    //待推送的走查询推送
                    dmpPushTaskService.batchFindDataSync(Arrays.asList(recordEntity.getId()));
                } else {
                    DmpPushTaskHistoryServiceImpl.sendMq(recordEntity.getMqData(), recordEntity.getId(),recordEntity.getVersion(), mqProducerService, recordEntity.getMqTopic(), recordEntity.getMqTag(), recordEntity.getSourceId());
                }
            } catch (Exception e){
                String sourceTypeName = SourceTypeEnum.getName(recordEntity.getSourceType());
                log.error("从{}推送{}到{}发送消息异常", recordEntity.getSourcePlatformName(), sourceTypeName, recordEntity.getTargetPlatformName(), e);
                XxlJobHelper.log("从{}推送{}到{}发送消息异常", recordEntity.getSourcePlatformName(), recordEntity.getSourceType(), recordEntity.getTargetPlatformName(),  e);
            }
        }
        //更新信息
        if (CollectionUtil.isNotEmpty(updateList)) {
            dmpPushTaskService.updateBatchById(updateList);
        }
        XxlJobHelper.log("DmpPushTaskJob end");
        return ReturnT.SUCCESS;
    }

    /**
     * 归档DMP推送同步任务
     * @return
     */
    @XxlJob("SyncPushTaskHistoryJob")
    public ReturnT<String> syncPushTaskHistory() {
        dmpPushTaskHistoryService.syncPushTaskHistory();
        return ReturnT.SUCCESS;
    }

    /**
     * 归档DMP推送同步任务
     * @return
     */
    @XxlJob("SyncPullTaskHistoryJob")
    public ReturnT<String> syncPullTaskHistory(Integer month) {
        dmpPullTaskHistoryService.syncPullTaskHistory(month);
        return ReturnT.SUCCESS;
    }

}