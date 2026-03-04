package com.erp.server.tms.service;
import com.erp.model.tms.entity.AsyncTaskRecordEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 异步任务记录 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
public interface AsyncTaskRecordService extends SuperService<AsyncTaskRecordEntity> {

    String addTask(String businessType, String json);

    void updateTask(String taskId,String status, String errorMsg);

    void updateTaskFinally(String taskId);
}
