package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.mapper.AsyncTaskDetailRecordMapper;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 异步任务记录明细 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
@Slf4j
@Service
public class TmsAsyncTaskDetailServiceImpl extends SuperServiceImpl<AsyncTaskDetailRecordMapper, TmsAsyncTaskDetailEntity> implements TmsAsyncTaskDetailService {



    @Override
    public void updateDetail(String taskDetailId, String status, String msg){
        lambdaUpdate()
                .set(TmsAsyncTaskDetailEntity::getStatus, status)
                .set(TmsAsyncTaskDetailEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskDetailEntity::getErrorData,msg)
                .eq(TmsAsyncTaskDetailEntity::getId,taskDetailId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean tryClaimDetailForExecution(String taskDetailId) {
        return lambdaUpdate()
            .set(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
            .set(TmsAsyncTaskDetailEntity::getStartTime, LocalDateTime.now())
            .set(TmsAsyncTaskDetailEntity::getErrorData, "")
            .eq(TmsAsyncTaskDetailEntity::getId, taskDetailId)
            .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
            .update();
    }

    /**
     * 将指定明细中仍未结束的数据标记为失败。
     *
     * @param detailIds 任务明细 ID 集合
     * @param errorMsg 失败原因
     * @return 实际标记失败的明细数量
     */
    @Override
    public int markDetailsFailed(Collection<String> detailIds, String errorMsg) {
        if (detailIds == null || detailIds.isEmpty()) {
            return 0;
        }
        List<String> validDetailIds = detailIds.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validDetailIds)) {
            return 0;
        }
        List<String> terminalStatuses = terminalStatuses();
        int unfinishedCount = lambdaQuery()
                .in(TmsAsyncTaskDetailEntity::getId, validDetailIds)
                .notIn(TmsAsyncTaskDetailEntity::getStatus, terminalStatuses)
                .count();
        if (unfinishedCount <= 0) {
            return 0;
        }
        lambdaUpdate()
                .set(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .set(TmsAsyncTaskDetailEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskDetailEntity::getErrorData, StringUtils.substring(errorMsg, 0, 1000))
                .in(TmsAsyncTaskDetailEntity::getId, validDetailIds)
                .notIn(TmsAsyncTaskDetailEntity::getStatus, terminalStatuses)
                .update();
        return unfinishedCount;
    }

    /**
     * 将指定业务 ID 中超过执行窗口的 ING 明细标记失败。
     *
     * @param mainId 主任务 ID
     * @param businessIds 业务 ID 集合
     * @param staleBefore 僵死阈值时间
     * @param errorMsg 失败原因
     * @return 实际标记失败的明细数量
     */
    @Override
    public int markStaleIngDetailsFailed(String mainId, Collection<String> businessIds, LocalDateTime staleBefore, String errorMsg) {
        if (StringUtils.isBlank(mainId) || businessIds == null || businessIds.isEmpty() || staleBefore == null) {
            return 0;
        }
        List<String> validBusinessIds = businessIds.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validBusinessIds)) {
            return 0;
        }
        int staleCount = lambdaQuery()
                .eq(TmsAsyncTaskDetailEntity::getMainId, mainId)
                .in(TmsAsyncTaskDetailEntity::getBusinessId, validBusinessIds)
                .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                .le(TmsAsyncTaskDetailEntity::getStartTime, staleBefore)
                .count();
        if (staleCount <= 0) {
            return 0;
        }
        lambdaUpdate()
                .set(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .set(TmsAsyncTaskDetailEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskDetailEntity::getErrorData, StringUtils.substring(errorMsg, 0, 1000))
                .eq(TmsAsyncTaskDetailEntity::getMainId, mainId)
                .in(TmsAsyncTaskDetailEntity::getBusinessId, validBusinessIds)
                .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                .le(TmsAsyncTaskDetailEntity::getStartTime, staleBefore)
                .update();
        return staleCount;
    }

    @Override
    public List<TmsAsyncTaskDetailEntity> listErrorDetail(String mainId){
        return lambdaQuery()
                .eq(TmsAsyncTaskDetailEntity::getMainId,mainId)
                .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .list();

    }

    /**
     * 错误重试只分页读取来源任务失败明细，避免大批量失败 ID 撑大 dataJson 和 MQ 消息体。
     */
    @Override
    public List<String> listFailedBusinessIdsByCursor(String mainId, String lastBusinessId, int batchSize) {
        if (StringUtils.isBlank(mainId)) {
            throw new ServiceException("错误重试来源任务不能为空");
        }
        int safeBatchSize = batchSize <= 0 ? 500 : batchSize;
        return lambdaQuery()
                .select(TmsAsyncTaskDetailEntity::getBusinessId)
                .eq(TmsAsyncTaskDetailEntity::getMainId, mainId)
                .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .isNotNull(TmsAsyncTaskDetailEntity::getBusinessId)
                .gt(StringUtils.isNotBlank(lastBusinessId), TmsAsyncTaskDetailEntity::getBusinessId, lastBusinessId)
                .orderByAsc(TmsAsyncTaskDetailEntity::getBusinessId)
                .last("LIMIT " + safeBatchSize)
                .list()
                .stream()
                .map(TmsAsyncTaskDetailEntity::getBusinessId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void saveBatchInChunks(List<TmsAsyncTaskDetailEntity> details, int batchSize) {
        if (details == null || details.isEmpty()) {
            return;
        }
        int safeBatchSize = batchSize <= 0 ? 500 : batchSize;
        for (int i = 0; i < details.size(); i += safeBatchSize) {
            saveBatch(details.subList(i, Math.min(i + safeBatchSize, details.size())), safeBatchSize);
        }
    }

    @Override
    public List<String> listExistingBusinessIds(String mainId, Collection<String> businessIds) {
        if (StringUtils.isBlank(mainId) || businessIds == null || businessIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .select(TmsAsyncTaskDetailEntity::getBusinessId)
                .eq(TmsAsyncTaskDetailEntity::getMainId, mainId)
                .in(TmsAsyncTaskDetailEntity::getBusinessId, businessIds)
                .list()
                .stream()
                .map(TmsAsyncTaskDetailEntity::getBusinessId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<TmsAsyncTaskDetailEntity> listFailedDetailsByCursor(String mainId, String lastBusinessId, int batchSize) {
        if (StringUtils.isBlank(mainId)) {
            throw new ServiceException("错误重试来源任务不能为空");
        }
        int safeBatchSize = batchSize <= 0 ? 500 : batchSize;
        return lambdaQuery()
                .select(TmsAsyncTaskDetailEntity::getBusinessId, TmsAsyncTaskDetailEntity::getBusinessCode)
                .eq(TmsAsyncTaskDetailEntity::getMainId, mainId)
                .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .isNotNull(TmsAsyncTaskDetailEntity::getBusinessId)
                .gt(StringUtils.isNotBlank(lastBusinessId), TmsAsyncTaskDetailEntity::getBusinessId, lastBusinessId)
                .orderByAsc(TmsAsyncTaskDetailEntity::getBusinessId)
                .last("LIMIT " + safeBatchSize)
                .list();
    }

    @Override
    public int markUnfinishedBatchDetailsFailed(List<TmsAsyncTaskDetailEntity> taskDetailList, String errorMsg) {
        if (CollUtil.isEmpty(taskDetailList)) {
            return 0;
        }
        List<String> detailIds = taskDetailList.stream()
                .map(TmsAsyncTaskDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(detailIds)) {
            return 0;
        }
        List<String> terminalStatuses = terminalStatuses();
        int unfinishedCount = lambdaQuery()
                .in(TmsAsyncTaskDetailEntity::getId, detailIds)
                .notIn(TmsAsyncTaskDetailEntity::getStatus, terminalStatuses)
                .count();
        if (unfinishedCount <= 0) {
            return 0;
        }
        lambdaUpdate()
                .set(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .set(TmsAsyncTaskDetailEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskDetailEntity::getErrorData, StringUtils.substring(errorMsg, 0, 1000))
                .in(TmsAsyncTaskDetailEntity::getId, detailIds)
                .notIn(TmsAsyncTaskDetailEntity::getStatus, terminalStatuses)
                .update();
        return unfinishedCount;
    }

    private List<String> terminalStatuses() {
        return Arrays.asList(
                TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                TmsAsyncTaskRecordStatusEnum.FAILED.getCode());
    }

}
