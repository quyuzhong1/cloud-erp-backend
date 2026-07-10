package com.erp.server.tms.handler.asynctask;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TransferDeclareCostAllocationMainReportStatusEnum;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.TransferDeclareCostAllocationMainService;
import com.erp.server.tms.service.asynctask.TransferDeclareCostAllocationAsyncTaskDelegate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class TransferReAllocationBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.TransferDeclareReportPeriodBatchPayloadDTO> {

    @Lazy
    @Resource
    private TransferDeclareCostAllocationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    @Override
    public String taskDisplayName() {
        return "中转重新分摊异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.TransferDeclareReportPeriodBatchPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.TransferDeclareReportPeriodBatchPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "中转重新分摊异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.TransferDeclareReportPeriodBatchPayloadDTO payload) {
        if (CharSequenceUtil.isBlank(payload.getReportPeriodStr())) {
            return "核算期间为空";
        }
        return null;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                     TmsAsyncTaskRecordDTO.TransferDeclareReportPeriodBatchPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        String reportStatus = CharSequenceUtil.blankToDefault(
            payload.getReportStatus(), TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode());
        BatchBusinessIdProvider defaultProvider = (cursor, size) ->
            transferDeclareCostAllocationMainService.pageMainIdsForReAllocation(
                payload.getReportPeriodStr(), reportStatus, cursor, size);
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.TransferDeclareReportPeriodBatchPayloadDTO payload,
                                                                LoginUser operatorUser,
                                                                List<String> batchIds,
                                                                int batchNumber,
                                                                CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        return asyncTaskDelegate.processReAllocationBatch(
            taskId, batchIds, timeoutSeconds, staleDetailSeconds, operatorUser);
    }
}
