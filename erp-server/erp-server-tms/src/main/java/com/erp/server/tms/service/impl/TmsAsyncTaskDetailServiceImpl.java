package com.erp.server.tms.service.impl;

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
            .set(TmsAsyncTaskDetailEntity::getErrorData, "")
            .eq(TmsAsyncTaskDetailEntity::getId, taskDetailId)
            .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
            .update();
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

}
