package com.erp.server.tms.handler.asynctask;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.SmallBagCostAllocationMainReportStatusEnum;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.SmallBagCostAllocationMainService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.SmallBagCostAllocationAsyncTaskDelegate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SmallBagDeleteBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO> {

    @Lazy
    @Resource
    private SmallBagCostAllocationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;

    @Override
    public String taskDisplayName() {
        return "小包批量删除异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "小包批量删除异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO payload) {
        if (CharSequenceUtil.isBlank(payload.getReportPeriodStr())) {
            return "核算期间为空";
        }
        return null;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                   TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                   TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO payload,
                                   String lastId,
                                   int batchSize,
                                   TmsAsyncTaskRecordEntity taskRecord) {
        String reportStatus = CharSequenceUtil.blankToDefault(
            payload.getReportStatus(), SmallBagCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode());
        BatchBusinessIdProvider defaultProvider = (cursor, size) ->
            smallBagCostAllocationMainService.pageMainIdsForReAllocation(
                payload.getReportPeriodStr(), reportStatus, cursor, size);
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO payload,
                                                                LoginUser operatorUser,
                                                                List<String> batchIds,
                                                                int batchNumber,
                                                                CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        return asyncTaskDelegate.processDeleteBatch(
            taskId, batchIds, timeoutSeconds, staleDetailSeconds, operatorUser);
    }
}
