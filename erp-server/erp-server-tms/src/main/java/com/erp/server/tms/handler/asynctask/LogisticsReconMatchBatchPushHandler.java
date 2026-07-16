package com.erp.server.tms.handler.asynctask;

import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流商对账单合并匹配异步任务分批执行策略。
 * <p>一主单一任务：payload 存主单 id；任务明细 businessId 为费用项 id，按游标分批认领匹配。</p>
 */
@Component
public class LogisticsReconMatchBatchPushHandler
        implements TmsAsyncTaskBatchPushHandler<TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO> {

    @Lazy
    @Resource
    private LogisticsReconService logisticsReconService;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private TmsAsyncTaskDetailService tmsAsyncTaskDetailService;

    @Override
    public String taskDisplayName() {
        return "物流商对账单合并匹配异步任务";
    }

    @Override
    public Class<TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO> payloadClass() {
        return TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO.class;
    }

    @Override
    public String payloadParseErrorMessage() {
        return "物流商对账单合并匹配异步任务信封参数解析失败";
    }

    @Override
    public String validatePayload(TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO payload) {
        if (payload == null || StringUtils.isBlank(payload.resolveMainId())) {
            return "无可匹配的对账单";
        }
        return null;
    }

    @Override
    public List<String> pageBatchIds(String taskId,
                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                     TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO payload,
                                     String lastId,
                                     int batchSize,
                                     TmsAsyncTaskRecordEntity taskRecord) {
        // 正常路径：按本任务待执行明细（费用项 id）游标分页；失败重试走来源任务失败明细
        BatchBusinessIdProvider defaultProvider = (cursor, size) -> pagePendingDetailBusinessIds(taskId, cursor, size);
        return asyncTaskRecordService.pageBatchBusinessIds(
                envelope.getRetryMode(), envelope.getRetrySourceTaskId(), lastId, batchSize,
                defaultProvider, null);
    }

    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                 TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                 TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO payload,
                                                                 LoginUser operatorUser,
                                                                 List<String> batchIds,
                                                                 int batchNumber,
                                                                 CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
        return logisticsReconService.processMatchBatch(taskId, payload.resolveMainId(), batchIds,
                Boolean.TRUE.equals(payload.getIsConfirm()), operatorUser);
    }

    private List<String> pagePendingDetailBusinessIds(String taskId, String cursor, int batchSize) {
        if (StringUtils.isBlank(taskId)) {
            return Collections.emptyList();
        }
        int size = batchSize > 0 ? batchSize : 500;
        List<String> ids = tmsAsyncTaskDetailService.lambdaQuery()
                .select(TmsAsyncTaskDetailEntity::getBusinessId)
                .eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
                .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                .gt(StringUtils.isNotBlank(cursor), TmsAsyncTaskDetailEntity::getBusinessId, cursor)
                .orderByAsc(TmsAsyncTaskDetailEntity::getBusinessId)
                .last("LIMIT " + size)
                .list()
                .stream()
                .map(TmsAsyncTaskDetailEntity::getBusinessId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        // 首批即空且任务无任何明细：多为升级前旧任务（payload 仅主单 ids、未预落费用项明细）
        if (ids.isEmpty() && StringUtils.isBlank(cursor)) {
            int detailTotal = tmsAsyncTaskDetailService.lambdaQuery()
                    .eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
                    .count();
            if (detailTotal == 0) {
                throw new ServiceException("任务无费用项明细（可能为升级前旧任务），请终止后重新提交");
            }
        }
        return ids;
    }
}
