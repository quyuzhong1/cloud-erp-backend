package com.erp.server.tms.service.support;

import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.message.constant.DistributeKeyConstant;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.service.TmsAsyncTaskBatchDetailPushHandler;
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

    /**
     * 与未迁移 push 消费保持一致，防止游标异常导致无限循环。
     */
    private static final int MAX_BATCH_LIMIT = 2000;

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
                asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                    ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
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

            if (!claimTaskIfPending(taskId, handler.taskDisplayName(), currentRecord)) {
                return;
            }

            int batchSize = asyncTaskRecordService.resolveBatchSize(billBatchParams.getBatch(), 500);
            LoginUser operatorUser = asyncTaskRecordService.resolveOperatorLoginUser(taskRecord, envelope);
            String lastId = "";
            int totalProcessed = 0;
            int totalSuccess = 0;
            int totalFailed = 0;
            int batchNumber = 0;
            int detailCount = Objects.isNull(taskRecord.getDetailCount()) ? 0 : taskRecord.getDetailCount();

            log.info("开始分批处理{}，taskId: {}, 批次大小: {}, 预计总数: {}",
                handler.taskDisplayName(), taskId, batchSize, currentRecord.getDetailCount());

            boolean skipFinallyUpdate = false;
            while (true) {
                batchNumber++;

                if (batchNumber > MAX_BATCH_LIMIT) {
                    log.warn("{}超过最大批次数，强制退出，taskId: {}, maxBatchLimit: {}",
                        handler.taskDisplayName(), taskId, MAX_BATCH_LIMIT);
                    asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                        "超过最大批次数，强制退出");
                    break;
                }

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
                    if (totalProcessed == 0) {
                        String emptyFirstBatchMessage = handler.emptyFirstBatchMessage(envelope, payload);
                        if (StringUtils.isNotBlank(emptyFirstBatchMessage)) {
                            asyncTaskRecordService.updateTask(taskId,
                                TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), emptyFirstBatchMessage);
                            skipFinallyUpdate = true;
                            break;
                        }
                    }
                    log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/成功: {}/失败: {}",
                        taskId, batchNumber - 1, totalProcessed, totalSuccess, totalFailed);
                    break;
                }

                TmsAsyncTaskRecordDTO.BatchProcessResult result = handler.processBatch(
                    taskId, envelope, payload, operatorUser, batchIds, batchNumber, billBatchParams);

                totalProcessed += batchIds.size();
                totalSuccess += result.getSuccessCount();
                totalFailed += result.getFailedCount();

                // 预计总量仅在创建时写入；循环内只回写失败数，结束时由 updateTaskFinally 统计 detailCount
                if (detailCount > 0 && totalProcessed >= detailCount) {
                    break;
                }
                try {
                    asyncTaskRecordService.lambdaUpdate()
                        .set(TmsAsyncTaskRecordEntity::getErrorCount, totalFailed)
                        .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                        .update();
                } catch (Exception e) {
                    log.error("更新任务进度失败，taskId: {}", taskId, e);
                }

                lastId = batchIds.get(batchIds.size() - 1);
            }

            finishTaskUnlessSkipped(taskId, handler.taskDisplayName(), skipFinallyUpdate);
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

    /**
     * 明细先行模式：prepare 阶段创建/幂等写入任务明细后再并发执行。
     */
    public <P> void executePreparedDetails(TmsAsyncTaskRecordEntity taskRecord,
                                           TmsAsyncTaskBatchDetailPushHandler<P> handler) {
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
                asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                    ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
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

            String businessType = handler.resolveBusinessType(envelope, taskRecord, payload);
            if (StringUtils.isBlank(businessType)) {
                asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "业务类型为空");
                return;
            }

            if (!claimTaskIfPending(taskId, handler.taskDisplayName(), currentRecord)) {
                return;
            }

            int batchSize = asyncTaskRecordService.resolveBatchSize(billBatchParams.getBatch(), 500);
            String cursor = "";
            int totalProcessed = 0;
            int totalFailed = 0;
            int batchNumber = 0;
            int detailCount = Objects.isNull(taskRecord.getDetailCount()) ? 0 : taskRecord.getDetailCount();

            log.info("开始分批处理{}，taskId: {}, 批次大小: {}, 预计总数: {}",
                handler.taskDisplayName(), taskId, batchSize, currentRecord.getDetailCount());

            boolean skipFinallyUpdate = false;
            while (true) {
                batchNumber++;

                if (batchNumber > MAX_BATCH_LIMIT) {
                    log.warn("{}超过最大批次数，强制退出，taskId: {}, maxBatchLimit: {}",
                        handler.taskDisplayName(), taskId, MAX_BATCH_LIMIT);
                    asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                        "超过最大批次数，强制退出");
                    break;
                }

                if (batchNumber == 1 || batchNumber % 10 == 0) {
                    TmsAsyncTaskRecordEntity loopTask = asyncTaskRecordService.getById(taskId);
                    if (asyncTaskRecordService.shouldStopLoopTask(taskId, loopTask)) {
                        break;
                    }
                    if (asyncTaskRecordService.terminateTaskIfExecTimeoutReached(loopTask, billBatchParams)) {
                        break;
                    }
                }

                List<TmsAsyncTaskDetailEntity> batchDetails;
                try {
                    batchDetails = handler.prepareBatchDetails(
                        taskId, businessType, envelope, payload, cursor, batchSize);
                } catch (Exception e) {
                    log.error("第{}批查询失败，taskId: {}", batchNumber, taskId, e);
                    asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                        "第" + batchNumber + "批查询失败: " + asyncTaskRecordService.formatTaskErrorMessage(e));
                    break;
                }

                if (CollUtil.isEmpty(batchDetails)) {
                    if (totalProcessed == 0) {
                        String emptyFirstBatchMessage = handler.emptyFirstBatchMessage(envelope, payload);
                        if (StringUtils.isNotBlank(emptyFirstBatchMessage)) {
                            asyncTaskRecordService.updateTask(taskId,
                                TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), emptyFirstBatchMessage);
                            skipFinallyUpdate = true;
                            break;
                        }
                    }
                    log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/失败: {}",
                        taskId, batchNumber - 1, totalProcessed, totalFailed);
                    break;
                }

                TmsAsyncTaskRecordDTO.BatchProcessResult result = handler.processPreparedBatch(
                    taskId, envelope, payload, batchDetails, batchNumber, billBatchParams);

                totalProcessed += batchDetails.size();
                totalFailed += result.getFailedCount();
                // 预计总量仅在创建时写入；循环内只回写失败数，结束时由 updateTaskFinally 统计 detailCount
                if (detailCount > 0 && totalProcessed >= detailCount) {
                    break;
                }

                try {
                    asyncTaskRecordService.lambdaUpdate()
                        .set(TmsAsyncTaskRecordEntity::getErrorCount, totalFailed)
                        .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                        .update();
                } catch (Exception e) {
                    log.error("更新任务进度失败，taskId: {}", taskId, e);
                }

                String lastBusinessId = batchDetails.get(batchDetails.size() - 1).getBusinessId();
                if (StringUtils.isBlank(lastBusinessId)) {
                    log.error("{}批次末尾 businessId 为空，终止循环，taskId: {}",
                        handler.taskDisplayName(), taskId);
                    asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                        "批次末尾 businessId 为空，终止循环");
                    break;
                }
                cursor = lastBusinessId;
                log.info("{}第{}批完成，taskId: {}, 本批数量: {}, 失败: {}",
                    handler.taskDisplayName(), batchNumber, taskId, batchDetails.size(), result.getFailedCount());
            }

            finishTaskUnlessSkipped(taskId, handler.taskDisplayName(), skipFinallyUpdate);
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

    private boolean claimTaskIfPending(String taskId, String taskDisplayName, TmsAsyncTaskRecordEntity currentRecord) {
        if (Objects.equals(currentRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode())) {
            boolean claimed = asyncTaskRecordService.lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                .set(TmsAsyncTaskRecordEntity::getErrorData, ApiError.COMMON_BATCH_PROCESSING.getMsg())
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                .update();
            if (!claimed) {
                log.warn("{}已被其他消费者认领，taskId: {}", taskDisplayName, taskId);
                return false;
            }
        } else if (!Objects.equals(currentRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())) {
            log.warn("{}状态不可执行，taskId: {}, status: {}", taskDisplayName, taskId, currentRecord.getStatus());
            return false;
        }
        return true;
    }

    private void finishTaskUnlessSkipped(String taskId, String taskDisplayName, boolean skipFinallyUpdate) {
        if (skipFinallyUpdate) {
            return;
        }
        try {
            asyncTaskRecordService.updateTaskFinally(taskId);
            log.info("{}最终状态更新完成，taskId: {}", taskDisplayName, taskId);
        } catch (Exception e) {
            log.error("更新任务最终状态失败，taskId: {}", taskId, e);
        }
    }
}
