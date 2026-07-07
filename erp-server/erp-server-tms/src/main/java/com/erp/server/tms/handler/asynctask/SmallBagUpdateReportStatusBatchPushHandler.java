package com.erp.server.tms.handler.asynctask;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.SmallBagCostAllocationBigTableStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationReportStatusEnum;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.SmallBagCostAllocationMainService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.SmallBagCostAllocationAsyncTaskDelegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SmallBagUpdateReportStatusBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO> {

    @Lazy
    @Resource
    private SmallBagCostAllocationAsyncTaskDelegate asyncTaskDelegate;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;

    @Override
    public String taskDisplayName() {
        return "小包核算状态变更异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "小包核算状态变更异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO payload) {
        if (CharSequenceUtil.isBlank(payload.getReportPeriodStr())) {
            return "核算期间为空";
        }
        if (StringUtils.isBlank(payload.getReportDate()) && StringUtils.isBlank(payload.getReportStatus())) {
            return"会计期间和核算状态不能同时为空";
        }
        return null;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                   TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                   TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO payload,
                                   String lastId,
                                   int batchSize,
                                   TmsAsyncTaskRecordEntity taskRecord) {
        boolean excludeBigTableDone = SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode()
            .equals(payload.getReportStatus());
        String bigTableDoneCode = SmallBagCostAllocationBigTableStatusEnum.DONE.getCode();
        BatchBusinessIdProvider defaultProvider = (cursor, size) ->
            smallBagCostAllocationMainService.pageMainIdsByReportPeriodStr(
                payload.getReportPeriodStr(), payload.getReportStatus(), excludeBigTableDone, bigTableDoneCode,
                cursor, size);
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO payload,
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
