package com.erp.server.tms.handler.asynctask;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.FirstMileCostAllocationAsyncTaskDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
@Slf4j
@Component
public class FirstMilePushAllocationBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO> {

    @Lazy
    @Resource
    private FirstMileCostAllocationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;

    @Override
    public String taskDisplayName() {
        return "头程分摊异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "头程分摊异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO payload) {
        if (CharSequenceUtil.isBlank(payload.getReportDate())) {
            return "核算日期为空";
        }
        return null;
    }

    @Override
    public boolean refreshRecordBeforeClaim() {
        return false;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                     TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        BatchBusinessIdProvider defaultProvider = (cursor, size) -> {
            TmsAsyncTaskRecordDTO.FirstMilePushAllocationQueryDTO queryDTO =
                new TmsAsyncTaskRecordDTO.FirstMilePushAllocationQueryDTO(cursor, size);
            return firstMileWeightAllocationService.pageFirstMileDeliveryIds(queryDTO);
        };
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO payload,
                                                                LoginUser operatorUser,
                                                                List<String> batchIds,
                                                                int batchNumber,
                                                                CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        log.info("开始处理第{}批，数量: {}", batchNumber, batchIds.size());
        TmsAsyncTaskRecordDTO.BatchProcessResult result = asyncTaskDelegate.processPushAllocationBatch(
            taskId, batchIds, payload.getReportDate(), timeoutSeconds, staleDetailSeconds, operatorUser);
        log.info("第{}批完成，本批成功: {}/失败: {}",
            batchNumber, result.getSuccessCount(), result.getFailedCount());
        return result;
    }
}
