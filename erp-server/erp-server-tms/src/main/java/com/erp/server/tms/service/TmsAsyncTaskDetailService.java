package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 异步任务记录明细 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
public interface TmsAsyncTaskDetailService extends SuperService<TmsAsyncTaskDetailEntity> {

    void updateDetail(String taskDetailId, String status, String msg);

    /**
     * PENDING 认领为 ING，防止重复执行
     */
    boolean tryClaimDetailForExecution(String taskDetailId);

    List<TmsAsyncTaskDetailEntity> listErrorDetail(String mainId);
}
