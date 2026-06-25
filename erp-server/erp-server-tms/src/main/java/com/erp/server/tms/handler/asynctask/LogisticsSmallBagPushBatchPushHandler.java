package com.erp.server.tms.handler.asynctask;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.asynctask.LogisticsBillCostAsyncTaskDelegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 小包下推分摊异步任务 Handler（任务级有状态，由 Factory 按次创建）。
 */
@Slf4j
public class LogisticsSmallBagPushBatchPushHandler
    implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO> {

    private final LogisticsBillCostAsyncTaskDelegate asyncTaskDelegate;
    private final TmsAsyncTaskRecordService asyncTaskRecordService;
    private final LogisticsBillCostService logisticsBillCostService;

    private LogisticsBillCostDTO.SmallBagPushAllocationContext pushContext;

    public LogisticsSmallBagPushBatchPushHandler(LogisticsBillCostAsyncTaskDelegate asyncTaskDelegate,
                                                 TmsAsyncTaskRecordService asyncTaskRecordService,
                                                 LogisticsBillCostService logisticsBillCostService) {
        this.asyncTaskDelegate = asyncTaskDelegate;
        this.asyncTaskRecordService = asyncTaskRecordService;
        this.logisticsBillCostService = logisticsBillCostService;
    }

    @Override
    public String taskDisplayName() {
        return "小包分摊异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "小包分摊异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO payload) {
        if (StringUtils.isBlank(payload.getReportDate())) {
            return "核算日期为空";
        }
        if (StringUtils.isBlank(payload.getType())) {
            return "费用类型不能为空";
        }
        if (!Objects.equals(payload.getType(), DictCostAttributionEnum.SELF_DELIVER.getCode())
            && !Objects.equals(payload.getType(), DictCostAttributionEnum.LAST_MILE.getCode())) {
            return "仅支持自发货/尾程费用下推分摊";
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
                                     TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        BatchBusinessIdProvider defaultProvider = (cursor, size) -> {
            LogisticsBillCostDTO.CanPushAllocationPageQueryDTO query =
                new LogisticsBillCostDTO.CanPushAllocationPageQueryDTO(
                    payload.getReportDate(), payload.getType(), cursor, size, null);
            return logisticsBillCostService.pageByCanPushAllocation(query);
        };
        return asyncTaskRecordService.pageBatchBusinessIds(
            envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
            defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO payload,
                                                                LoginUser operatorUser,
                                                                List<String> batchIds,
                                                                int batchNumber,
                                                                CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        int timeoutSeconds = asyncTaskRecordService.resolveTimeoutSeconds(
            billBatchParams.getBatchTimeoutSeconds(), 5000);
        int staleDetailSeconds = asyncTaskRecordService.resolveStaleDetailSeconds(billBatchParams);
        if (pushContext == null) {
            pushContext = asyncTaskDelegate.buildSmallBagPushAllocationContext();
        }
        log.info("开始处理第{}批，数量: {}", batchNumber, batchIds.size());
        TmsAsyncTaskRecordDTO.BatchProcessResult result = asyncTaskDelegate.processPushAllocationBatch(
            taskId, envelope.getBusinessType(), batchIds, payload.getReportDate(),
            timeoutSeconds, staleDetailSeconds, pushContext);
        log.info("第{}批完成，本批成功: {}/失败: {}",
            batchNumber, result.getSuccessCount(), result.getFailedCount());
        return result;
    }
}
