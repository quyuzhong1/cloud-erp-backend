package com.erp.server.tms.handler.asynctask;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TransferDeclareCostAllocationBigTableStatusEnum;
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
public class TransferUpdateReportStatusBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.TransferDeclareUpdateReportStatusPayloadDTO> {

    @Lazy
    @Resource
    private TransferDeclareCostAllocationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    @Override
    public String taskDisplayName() {
        return "中转核算状态变更异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.TransferDeclareUpdateReportStatusPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.TransferDeclareUpdateReportStatusPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "中转核算状态变更异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.TransferDeclareUpdateReportStatusPayloadDTO payload) {
        if (CharSequenceUtil.isBlank(payload.getReportPeriodStr())) {
            return "核算期间为空";
        }
        return null;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                     TmsAsyncTaskRecordDTO.TransferDeclareUpdateReportStatusPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        boolean excludeBigTableDone = TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode()
            .equals(payload.getReportStatus());
        String bigTableDoneCode = TransferDeclareCostAllocationBigTableStatusEnum.DONE.getCode();
        BatchBusinessIdProvider defaultProvider = (cursor, size) ->
            transferDeclareCostAllocationMainService.pageMainIdsByReportPeriodStr(
                payload.getReportPeriodStr(), payload.getReportStatus(), excludeBigTableDone, bigTableDoneCode,
                cursor, size);
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.TransferDeclareUpdateReportStatusPayloadDTO payload,
                                                                LoginUser operatorUser,
                                                                List<String> batchIds,
                                                                int batchNumber,
                                                                CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        return asyncTaskDelegate.processUpdateStatusBatch(
            taskId, batchIds, payload.getReportDate(), payload.getReportStatus(),
            timeoutSeconds, staleDetailSeconds, operatorUser);
    }
}
