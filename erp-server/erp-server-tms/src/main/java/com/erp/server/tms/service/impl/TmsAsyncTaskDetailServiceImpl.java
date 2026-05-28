package com.erp.server.tms.service.impl;

import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.mapper.AsyncTaskDetailRecordMapper;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

}
