package com.erp.server.workflow.service;

import com.common.business.dto.FindUserDTO;
import com.erp.model.workflow.entity.ProcessTaskCcEntity;
import com.common.business.service.SuperService;

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
     * @param taskId
     * @param copyUser
     */
    void saveCcUser(String taskId, List<FindUserDTO> copyUser);
}
