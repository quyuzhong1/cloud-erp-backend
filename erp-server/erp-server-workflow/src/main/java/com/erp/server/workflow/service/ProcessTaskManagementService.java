package com.erp.server.workflow.service;

import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.common.business.service.SuperService;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
public interface ProcessTaskManagementService extends SuperService<ProcessTaskManagementEntity> {

    /**
     * 更新审批状态
     *
     * @param taskId
     * @param approveType
     * @param comment
     * @return Boolean
     */
    Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String comment);

    /**
     * 根据流程实例id查询历史任务
     *
     * @param processInstanceId
     * @param num 返回任务数量
     * @return 以节点id为key，任务列表为value的map
     */
    LinkedHashMap<String, List<ProcessTaskManagementEntity>> listHisByProcessInstanceId(String processInstanceId, Integer num);
}
