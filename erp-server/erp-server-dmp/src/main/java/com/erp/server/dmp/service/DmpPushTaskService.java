package com.erp.server.dmp.service;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 中台同步任务表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
public interface DmpPushTaskService extends SuperService<DmpPushTaskEntity> {


    /**
     * 发送mq并保存任务
     * @param dto
     */
    void sendMqAndSaveTask(DmpPushTaskFeignDTO dto);

    /**
     * 更新同步
     * @param id
     * @param status
     * @param msg
     */
    void updateStatus(String id, String status, String msg);
}
