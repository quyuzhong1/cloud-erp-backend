package com.cloud.erp.workflow.service;

import com.cloud.erp.workflow.dto.ApproveProcessRejectDTO;
import com.cloud.erp.workflow.dto.StartProcessDTO;

/**
 * @Classname ProcessInstanceService
 * @Description TODO
 * @Date 2022-08-10 15:33
 * @Created by yl
 */
public interface ProcessInstanceService {

    void startProcessInstanceByKey(StartProcessDTO dto);

    void reject(ApproveProcessRejectDTO dto);

    void rejectBack(ApproveProcessRejectDTO dto);
}
