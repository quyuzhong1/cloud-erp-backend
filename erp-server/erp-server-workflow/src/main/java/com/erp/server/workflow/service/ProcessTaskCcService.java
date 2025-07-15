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
     * @param taskId
     */
    void updateCcStatus(String taskId);

    /**
     * 转移抄送人
     * @param entityList
     * @param insertEntity
     */
    void updateCcTransfer(List<ProcessTaskManagementEntity> entityList, ProcessTaskManagementEntity insertEntity);

    /**
     * 发送抄送消息
     * @param entity
     * @param title
     * @param content
     */
    void sendCcMsg(ProcessTaskManagementEntity entity,String title,String content);

    /**
     *
     * @param taskManagementIds
     */
    List<ProcessTaskCcEntity> listTackCc(List<String> taskManagementIds);
}
