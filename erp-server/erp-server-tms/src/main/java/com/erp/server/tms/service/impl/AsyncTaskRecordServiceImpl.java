package com.erp.server.tms.service.impl;

import com.erp.model.tms.entity.AsyncTaskDetailRecordEntity;
import com.erp.model.tms.entity.AsyncTaskRecordEntity;
import com.erp.model.tms.enums.AsyncTaskRecordStatusEnum;
import com.erp.server.tms.mapper.AsyncTaskRecordMapper;
import com.erp.server.tms.service.AsyncTaskDetailRecordService;
import com.erp.server.tms.service.AsyncTaskRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 异步任务记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
@Slf4j
@Service
public class AsyncTaskRecordServiceImpl extends SuperServiceImpl<AsyncTaskRecordMapper, AsyncTaskRecordEntity> implements AsyncTaskRecordService {

    @Resource
    private AsyncTaskDetailRecordService asyncTaskDetailRecordService;

    @Override
    public String addTask(String businessType, String json){
        AsyncTaskRecordEntity entity = new AsyncTaskRecordEntity();
        entity.setBusinessType(businessType);
        entity.setDataJson(json);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
        return save(entity) ? entity.getId() : null;
    }

    @Override
    public void updateTask(String taskId,String status, String errorMsg) {
        lambdaUpdate()
                .set(AsyncTaskRecordEntity::getStatus, status)
                .set(AsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(AsyncTaskRecordEntity::getErrorData, errorMsg)
                .eq(AsyncTaskRecordEntity::getId, taskId);
    }

    @Override
    public void updateTaskFinally(String taskId) {
        AsyncTaskRecordEntity mainEntity = getById(taskId);
        if(Objects.nonNull(mainEntity)){
            Integer count = asyncTaskDetailRecordService.lambdaQuery().eq(AsyncTaskDetailRecordEntity::getMainId, taskId).ne(AsyncTaskDetailRecordEntity::getStatus,AsyncTaskRecordStatusEnum.ING.getCode()).count();
            if(Objects.equals(mainEntity.getDetailCount(), count)){
                Integer failedCount = asyncTaskDetailRecordService.lambdaQuery().eq(AsyncTaskDetailRecordEntity::getMainId, taskId).eq(AsyncTaskDetailRecordEntity::getStatus, AsyncTaskRecordStatusEnum.FAILED.getCode()).count();

                if(failedCount == 0){
                    this.updateTask(taskId,AsyncTaskRecordStatusEnum.SUCCESS.getCode(),"");
                }else if(failedCount > 0 && failedCount == count){
                    this.updateTask(taskId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"");
                }else if(failedCount > 0 && failedCount != count){
                    this.updateTask(taskId,AsyncTaskRecordStatusEnum.PART_SUCCESS.getCode(),"");
                }
            }
        }
    }
}
