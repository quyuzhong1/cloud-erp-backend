package com.erp.server.tms.service.support;

import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.message.constant.DistributeKeyConstant;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * TMS 异步任务 Pattern B 分批消费固定骨架。
 */
@Slf4j
@Component
public class TmsAsyncTaskBatchConsumerSupport {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    public <P> void execute(TmsAsyncTaskRecordEntity taskRecord, TmsAsyncTaskBatchPushHandler<P> handler) {
        if (taskRecord == null || StringUtils.isBlank(taskRecord.getId())) {
            log.error("{}任务ID为空", handler.taskDisplayName());
            return;
        }
        String taskId = taskRecord.getId();
        RLock taskLock = redissonClient.getLock(DistributeKeyConstant.TMS_ASYNC_TASK_EXEC_KEY + ":" + taskId);
        boolean locked = false;
        try {
            locked = taskLock.tryLock(0, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("{}正在执行，跳过重复消费，taskId: {}", handler.taskDisplayName(), taskId);
                return;
            }

            CfgSettingValueDTO.BillBatchParamsDTO billBatchParams = asyncTaskRecordService.loadBillBatchParams(taskId);
            if (billBatchParams == null) {
                return;
            }

            TmsAsyncTaskRecordEntity currentRecord = handler.refreshRecordBeforeClaim()
                ? asyncTaskRecordService.getById(taskId) : taskRecord;
            if (currentRecord == null) {
                log.error("任务记录不存在，taskId: {}", taskId);
                return;
            }
            if (Objects.equals(currentRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
                log.warn("{}已完成，跳过重复消费，taskId: {}", handler.taskDisplayName(), taskId);
                return;
            }

            TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope =
                asyncTaskRecordService.parseEnvelope(currentRecord.getDataJson());
            P payload = asyncTaskRecordService.parseEnvelopePayloadOrFinishTask(
                taskId, envelope, handler.payloadClass(), handler.payloadParseErrorMessage());
            if (envelope == null || payload == null) {
                return;
            }

            String validationError = handler.validatePayload(payload);
            if (StringUtils.isNotBlank(validationError)) {
                asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), validationError);
                return;
            }

            if (Objects.equals(currentRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode())) {
                boolean claimed = asyncTaskRecordService.lambdaUpdate()
                    .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                    .set(TmsAsyncTaskRecordEntity::getErrorData, ApiError.COMMON_BATCH_PROCESSING.getMsg())
                    .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                    .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                    .update();
                if (!claimed) {
                    log.warn("{}已被其他消费者认领，taskId: {}", handler.taskDisplayName(), taskId);
                    return;
                }
            } else if (!Objects.equals(currentRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())) {
                log.warn("{}状态不可执行，taskId: {}, status: {}",
                    handler.taskDisplayName(), taskId, currentRecord.getStatus());
                return;
            }

            int batchSize = asyncTaskRecordService.resolveBatchSize(billBatchParams.getBatch(), 500);
            LoginUser operatorUser = asyncTaskRecordService.resolveOperatorLoginUser(taskRecord, envelope);
            String lastId = "";
            int totalProcessed = 0;
            int totalSuccess = 0;
            int totalFailed = 0;
            int batchNumber = 0;

            log.info("开始分批处理{}，taskId: {}, 批次大小: {}, 预计总数: {}",
                handler.taskDisplayName(), taskId, batchSize, currentRecord.getDetailCount());

            while (true) {
                batchNumber++;

                if (batchNumber == 1 || batchNumber % 10 == 0) {
                    TmsAsyncTaskRecordEntity loopTask = asyncTaskRecordService.getById(taskId);
                    if (asyncTaskRecordService.shouldStopLoopTask(taskId, loopTask)) {
                        break;
                    }
                    if (asyncTaskRecordService.terminateTaskIfExecTimeoutReached(loopTask, billBatchParams)) {
                        break;
                    }
                }

                List<String> batchIds;
                try {
                    batchIds = handler.pageBatchIds(taskId, envelope, payload, lastId, batchSize, currentRecord);
                } catch (Exception e) {
                    log.error("第{}批查询失败，taskId: {}", batchNumber, taskId, e);
                    asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                        "第" + batchNumber + "批查询失败: " + asyncTaskRecordService.formatTaskErrorMessage(e));
                    break;
                }

                if (CollUtil.isEmpty(batchIds)) {
                    log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/成功: {}/失败: {}",
                        taskId, batchNumber - 1, totalProcessed, totalSuccess, totalFailed);
                    break;
                }

                TmsAsyncTaskRecordDTO.BatchProcessResult result = handler.processBatch(
                    taskId, envelope, payload, operatorUser, batchIds, batchNumber, billBatchParams);

                totalProcessed += batchIds.size();
                totalSuccess += result.getSuccessCount();
                totalFailed += result.getFailedCount();

                try {
                    asyncTaskRecordService.lambdaUpdate()
                        .set(TmsAsyncTaskRecordEntity::getErrorCount, totalFailed)
                        .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                        .update();
                } catch (Exception e) {
                    log.error("更新任务进度失败，taskId: {}", taskId, e);
                }

                handler.afterBatchProcessed(taskId, batchNumber, batchIds, result);
                lastId = batchIds.get(batchIds.size() - 1);
            }

            try {
                asyncTaskRecordService.updateTaskFinally(taskId);
                log.info("{}最终状态更新完成，taskId: {}", handler.taskDisplayName(), taskId);
            } catch (Exception e) {
                log.error("更新任务最终状态失败，taskId: {}", taskId, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("{}获取锁被中断，taskId: {}", handler.taskDisplayName(), taskId, e);
        } catch (Exception e) {
            log.error("{}执行失败，taskId: {}", handler.taskDisplayName(), taskId, e);
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                asyncTaskRecordService.formatTaskErrorMessage(e));
        } finally {
            if (locked && taskLock.isHeldByCurrentThread()) {
                taskLock.unlock();
            }
        }
    }
}
