package com.erp.server.tms.handler.asynctask;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.FirstMileCostAllocationAsyncTaskDelegate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class FirstMileUpdateStatusBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.FirstMileUpdateReportStatusPayloadDTO> {

    @Lazy
    @Resource
    private FirstMileCostAllocationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Override
    public String taskDisplayName() {
        return "头程核算状态变更异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.FirstMileUpdateReportStatusPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.FirstMileUpdateReportStatusPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "头程核算状态变更异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.FirstMileUpdateReportStatusPayloadDTO payload) {
        if (CharSequenceUtil.isBlank(payload.getReportPeriodStr())) {
            return "核算期间为空";
        }
        return null;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                     TmsAsyncTaskRecordDTO.FirstMileUpdateReportStatusPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        BatchBusinessIdProvider defaultProvider = (cursor, size) ->
            asyncTaskDelegate.pageIdsByReportPeriodMonth(
                asyncTaskDelegate.parseReportPeriodMonth(payload.getReportPeriodStr()),
                payload.getReportStatus(), cursor, size);
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.FirstMileUpdateReportStatusPayloadDTO payload,
                                                                LoginUser operatorUser,
                                                                List<String> batchIds,
                                                                int batchNumber,
                                                                CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        return asyncTaskDelegate.processUpdateStatusBatch(
            taskId, batchIds, payload.getReportStatus(), payload.getReportDate(),
            timeoutSeconds, staleDetailSeconds, operatorUser);
    }
}
