package com.erp.server.tms.handler.asynctask;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流商对账单账单确认异步任务分批执行策略。
 * <p>勾选的对账单主单 id 存于 payload，按游标分批后逐单调用既有 confirmBill 逻辑。</p>
 */
@Component
public class LogisticsReconConfirmBillBatchPushHandler
        implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO> {

    @Lazy
    @Resource
    private LogisticsReconService logisticsReconService;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Override
    public String taskDisplayName() {
        return "物流商对账单账单确认异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "物流商对账单账单确认异步任务信封参数解析失败";
    }

    /**
     * 校验账单确认 payload：对账单 id 与目标对账状态必填。
     */
    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO payload) {
        if (payload == null || payload.getIds() == null || payload.getIds().isEmpty()) {
            return "无可确认的对账单";
        }
        if (StringUtils.isBlank(payload.getReconciliationStatus())) {
            return "对账状态不能为空";
        }
        return null;
    }

    /**
     * 按 payload 内对账单 id 升序游标分批（支持失败重试模式）。
     */
    @Override
    public List<String> pageBatchIds(String taskId,
                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                     TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        List<String> ids = payload.getIds() == null ? Collections.emptyList() : payload.getIds();
        // 勾选 id 直接作为业务 id，按 id 游标升序分批（主单 id 为等长雪花串，字典序即自然序）
        BatchBusinessIdProvider defaultProvider = (cursor, size) -> ids.stream()
                .sorted()
                .filter(id -> StringUtils.isBlank(cursor) || id.compareTo(cursor) > 0)
                .limit(size)
                .collect(Collectors.toList());
        return asyncTaskRecordService.pageBatchBusinessIds(
                envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
                defaultProvider, null);
    }

    /**
     * 本批逐单执行账单确认，复用 {@link LogisticsReconService#processConfirmBillBatch}。
     */
    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                 TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                 TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO payload,
                                                                 LoginUser operatorUser,
                                                                 List<String> batchIds,
                                                                 int batchNumber,
                                                                 CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        return logisticsReconService.processConfirmBillBatch(taskId, batchIds,
                payload.getReconciliationStatus(), payload.getConfirmTime(), operatorUser);
    }
}
