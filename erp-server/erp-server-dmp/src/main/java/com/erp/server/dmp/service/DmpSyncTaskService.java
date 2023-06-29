package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 中台同步任务表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
public interface DmpSyncTaskService extends SuperService<DmpSyncTaskEntity> {


    /**
     * 更新出入库同步信息（成功/失败）
     * @param id
     * @param syncStatus
     * @param responseMsg
     */
    void updateSyncInfo(String id, String syncStatus, String responseMsg);


}
