package com.erp.server.workflow.service;

import com.common.business.dto.FindUserDTO;
import com.erp.model.workflow.entity.ProcessTaskCcEntity;
import com.common.business.service.SuperService;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-05-17
 */
public interface ProcessTaskCcService extends SuperService<ProcessTaskCcEntity> {

    /**
     * 保存抄送人
     *
     * @param taskId
     * @param copyUser
     * @param id
     */
    void saveCcUser(String taskId, List<FindUserDTO> copyUser, String taskManagementId);

    /**
     * 更新抄送状态
     * @param processInstanceId
     * @param taskId
     * @param id
     */
    void updateCcStatus(String processInstanceId, String taskId, String id);

    /**
     * 转移抄送人
     * @param entityList
     * @param insertEntity
     */
    void updateCcTransfer(List<ProcessTaskManagementEntity> entityList, ProcessTaskManagementEntity insertEntity);
}
