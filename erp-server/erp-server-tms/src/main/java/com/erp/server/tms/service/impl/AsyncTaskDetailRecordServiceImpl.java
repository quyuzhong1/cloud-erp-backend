package com.erp.server.tms.service.impl;

import com.erp.model.tms.entity.AsyncTaskDetailRecordEntity;
import com.erp.server.tms.mapper.AsyncTaskDetailRecordMapper;
import com.erp.server.tms.service.AsyncTaskDetailRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

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
public class AsyncTaskDetailRecordServiceImpl extends SuperServiceImpl<AsyncTaskDetailRecordMapper, AsyncTaskDetailRecordEntity> implements AsyncTaskDetailRecordService {



    @Override
    public void updateDetail(String taskDetailId, String status, String msg){
        lambdaUpdate()
                .set(AsyncTaskDetailRecordEntity::getStatus, status)
                .set(AsyncTaskDetailRecordEntity::getEndTime, LocalDateTime.now())
                .set(AsyncTaskDetailRecordEntity::getErrorData,msg)
                .eq(AsyncTaskDetailRecordEntity::getId,taskDetailId);
    }

}
